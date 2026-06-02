package com.verbally.app.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class SonioxAsyncTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val baseUrl: String = "https://api.soniox.com",
    private val pollDelayMillis: Long = 1_000,
    private val maxPollAttempts: Int = 120,
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey("Soniox"))

            var fileId: String? = null
            var transcriptionId: String? = null
            try {
                fileId = uploadFile(apiKey, audioFile)
                transcriptionId = createTranscription(apiKey, model, fileId)
                waitUntilCompleted(apiKey, transcriptionId)
                val text = getTranscript(apiKey, transcriptionId)
                if (text.isBlank()) throw ProviderException(messages.noTranscriptionText("Soniox"))
                RawTranscript(text = text, model = model, provider = "soniox")
            } finally {
                cleanupRemoteArtifacts(apiKey, transcriptionId, fileId)
            }
        }

    private fun uploadFile(apiKey: String, audioFile: File): String {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = "file",
                filename = audioFile.name,
                body = audioFile.asRequestBody(mediaTypeFor(audioFile).toMediaType()),
            )
            .build()

        val json = executeJson(
            Request.Builder()
                .url(endpoint("/v1/files"))
                .header("Authorization", "Bearer $apiKey")
                .post(body)
                .build(),
        )
        return json.optString("id").ifBlank {
            throw ProviderException(messages.responseParseFailed("Soniox"))
        }
    }

    private fun createTranscription(apiKey: String, model: String, fileId: String): String {
        val jsonBody = "{" +
            "\"model\":${jsonString(model)}," +
            "\"file_id\":${jsonString(fileId)}," +
            "\"enable_language_identification\":true" +
            "}"
        val json = executeJson(
            Request.Builder()
                .url(endpoint("/v1/transcriptions"))
                .header("Authorization", "Bearer $apiKey")
                .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build(),
        )
        return json.optString("id").ifBlank {
            throw ProviderException(messages.responseParseFailed("Soniox"))
        }
    }

    private suspend fun waitUntilCompleted(apiKey: String, transcriptionId: String) {
        repeat(maxPollAttempts) {
            val json = executeJson(
                Request.Builder()
                    .url(endpoint("/v1/transcriptions/$transcriptionId"))
                    .header("Authorization", "Bearer $apiKey")
                    .get()
                    .build(),
            )
            when (json.optString("status")) {
                "completed" -> return
                "error" -> {
                    val detail = json.optString("error_message")
                        .ifBlank { json.optString("error_type") }
                        .ifBlank { "error" }
                    throw ProviderException(messages.transcriptionFailed("Soniox", detail))
                }
            }
            if (pollDelayMillis > 0) delay(pollDelayMillis)
        }
        throw ProviderException(messages.transcriptionFailed("Soniox", "timeout"))
    }

    private fun getTranscript(apiKey: String, transcriptionId: String): String {
        val json = executeJson(
            Request.Builder()
                .url(endpoint("/v1/transcriptions/$transcriptionId/transcript"))
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build(),
        )
        return json.optString("text").trim()
    }

    private fun cleanupRemoteArtifacts(apiKey: String, transcriptionId: String?, fileId: String?) {
        val transcriptionDeleted = if (transcriptionId != null) {
            executeBestEffortDelete(endpoint("/v1/transcriptions/$transcriptionId"), apiKey)
        } else {
            false
        }
        if (!transcriptionDeleted && fileId != null) {
            executeBestEffortDelete(endpoint("/v1/files/$fileId"), apiKey)
        }
    }

    private fun executeBestEffortDelete(url: String, apiKey: String): Boolean =
        runCatching {
            httpClient.newCall(
                Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $apiKey")
                    .delete()
                    .build(),
            ).execute().use { it.isSuccessful }
        }.getOrDefault(false)

    private fun executeJson(request: Request): JSONObject {
        val response = httpClient.newCall(request).execute()
        response.use {
            val body = it.body.string()
            if (!it.isSuccessful) {
                throw ProviderException(messages.transcriptionFailed("Soniox", errorDetail(it.code, body)))
            }
            return runCatching { JSONObject(body) }.getOrElse {
                throw ProviderException(messages.responseParseFailed("Soniox"))
            }
        }
    }

    private fun errorDetail(code: Int, body: String): String {
        val json = runCatching { JSONObject(body) }.getOrNull()
        return json?.optString("message")?.takeIf { it.isNotBlank() }
            ?: json?.optString("error_message")?.takeIf { it.isNotBlank() }
            ?: "HTTP $code"
    }

    private fun endpoint(path: String): String = "${baseUrl.trimEnd('/')}$path"
}
