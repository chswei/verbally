package com.verbally.app.providers

import java.io.File
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class GeminiTranscriptionClient(
    private val httpClient: OkHttpClient = ProviderHttpClients.shared,
    private val baseUrl: String = "https://generativelanguage.googleapis.com",
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey(PROVIDER_NAME))

            createInteraction(apiKey, model, audioFile)
        }

    private fun createInteraction(
        apiKey: String,
        model: String,
        audioFile: File,
    ): RawTranscript {
        val mimeType = mediaTypeFor(audioFile)
        val input = JSONObject()
            .put("type", "audio")
            .put("data", Base64.getEncoder().encodeToString(audioFile.readBytes()))
            .put("mime_type", mimeType)
        val body = JSONObject()
            .put("model", model)
            .put("input", JSONArray().put(input))
            .toString()
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/v1beta/interactions")
            .header("x-goog-api-key", apiKey)
            .post(body.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        return httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body.string()
            if (!response.isSuccessful) {
                throw ProviderException(messages.transcriptionFailed(PROVIDER_NAME, response.code.toString()))
            }
            val text = parseInteractionText(responseBody)
            if (text.isBlank()) {
                noDictationRawTranscript(model, provider = PROVIDER_ID)
            } else {
                RawTranscript(text = text, model = model, provider = PROVIDER_ID)
            }
        }
    }

    private fun parseInteractionText(body: String): String =
        runCatching {
            val steps = JSONObject(body).getJSONArray("steps")
            val textParts = mutableListOf<String>()
            var foundTextContent = false
            for (stepIndex in 0 until steps.length()) {
                val step = steps.optJSONObject(stepIndex) ?: continue
                if (step.optString("type") != "model_output") continue
                val content = step.optJSONArray("content") ?: continue
                for (contentIndex in 0 until content.length()) {
                    val item = content.optJSONObject(contentIndex) ?: continue
                    if (item.optString("type") == "text" && item.has("text")) {
                        foundTextContent = true
                        textParts += item.optString("text")
                    }
                }
            }
            require(foundTextContent)
            textParts.joinToString("\n").trim()
        }.getOrElse {
            throw ProviderException(messages.responseParseFailed(PROVIDER_NAME))
        }

    private companion object {
        const val PROVIDER_NAME = "Gemini"
        const val PROVIDER_ID = "gemini"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
