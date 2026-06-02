package com.verbally.app.providers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SonioxAsyncTranscriptionClient(
    private val httpClient: OkHttpClient = ProviderHttpClients.shared,
    private val baseUrl: String = "https://api.soniox.com",
    private val pollDelayMillis: Long = 250,
    private val maxPollDelayMillis: Long = 1_000,
    private val maxPollAttempts: Int = 120,
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
    private val backgroundCleanup: ((() -> Unit) -> Unit) = { cleanup ->
        SonioxRemoteCleanupWork.launch(cleanup)
    },
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey("Soniox"))

            var fileId: String? = null
            var transcriptionId: String? = null
            var cleanupScheduled = false
            try {
                fileId = uploadFile(apiKey, audioFile)
                transcriptionId = createTranscription(apiKey, model, fileId)
                waitUntilCompleted(apiKey, transcriptionId)
                val transcript = getTranscript(apiKey, transcriptionId)
                cleanupScheduled = cleanupRemoteArtifactsInBackground(apiKey, transcriptionId, fileId)
                if (transcript.text.isBlank()) {
                    noDictationRawTranscript(model, provider = "soniox")
                } else {
                    RawTranscript(
                        text = transcript.text,
                        model = model,
                        provider = "soniox",
                        confidence = transcript.confidence,
                    )
                }
            } finally {
                if (!cleanupScheduled) {
                    cleanupRemoteArtifacts(apiKey, transcriptionId, fileId)
                }
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
        repeat(maxPollAttempts) { attemptIndex ->
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
            val delayMillis = pollDelayMillisFor(attemptIndex)
            if (delayMillis > 0 && attemptIndex < maxPollAttempts - 1) delay(delayMillis)
        }
        throw ProviderException(messages.transcriptionFailed("Soniox", "timeout"))
    }

    private fun pollDelayMillisFor(attemptIndex: Int): Long =
        (pollDelayMillis * (attemptIndex + 1))
            .coerceAtMost(maxPollDelayMillis)

    private fun getTranscript(apiKey: String, transcriptionId: String): SonioxTranscriptResult {
        val json = executeJson(
            Request.Builder()
                .url(endpoint("/v1/transcriptions/$transcriptionId/transcript"))
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build(),
        )
        return SonioxTranscriptResult(
            text = json.optString("text").trim(),
            confidence = confidenceFromTokens(json.optJSONArray("tokens")),
        )
    }

    private fun confidenceFromTokens(tokens: JSONArray?): TranscriptionConfidence? {
        if (tokens == null || tokens.length() == 0) return null

        var total = 0.0
        var count = 0
        for (index in 0 until tokens.length()) {
            val token = tokens.optJSONObject(index) ?: continue
            if (token.optBoolean("is_audio_event", false)) continue
            if (token.optString("translation_status") == "translation") continue
            if (token.optString("text").isBlank()) continue
            if (!token.has("confidence")) continue

            val confidence = token.optDouble("confidence", Double.NaN)
            if (confidence.isNaN() || confidence.isInfinite()) continue
            total += confidence.coerceIn(0.0, 1.0)
            count += 1
        }
        if (count == 0) return null

        return confidenceFromSonioxTokenAverage(total / count)
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

    private fun cleanupRemoteArtifactsInBackground(apiKey: String, transcriptionId: String?, fileId: String?): Boolean =
        runCatching {
            backgroundCleanup {
                cleanupRemoteArtifacts(apiKey, transcriptionId, fileId)
            }
        }.isSuccess

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

private object SonioxRemoteCleanupWork {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun launch(cleanup: () -> Unit) {
        scope.launch {
            cleanup()
        }
    }
}

private data class SonioxTranscriptResult(
    val text: String,
    val confidence: TranscriptionConfidence?,
)

private fun confidenceFromSonioxTokenAverage(confidence: Double): TranscriptionConfidence =
    when {
        confidence <= 0.2 -> TranscriptionConfidence.NONE
        confidence <= 0.6 -> TranscriptionConfidence.LOW
        confidence <= 0.85 -> TranscriptionConfidence.MEDIUM
        else -> TranscriptionConfidence.HIGH
    }
