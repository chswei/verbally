package com.verbally.app.providers

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiCleanupRequestFactory(
    private val baseUrl: String = "https://generativelanguage.googleapis.com",
) {
    fun create(
        apiKey: String,
        model: String,
        rawTranscript: String,
        cleanupPrompt: String = CleanupPromptFactory.defaultCleanupPrompt,
    ): Request {
        val prompt = CleanupPromptFactory.cleanupPrompt(cleanupPrompt, rawTranscript)
        val json = """
            {
              "contents": [
                {
                  "parts": [
                    { "text": ${jsonString(prompt)} }
                  ]
                }
              ]
            }
        """.trimIndent()

        return Request.Builder()
            .url("${baseUrl.trimEnd('/')}/v1beta/models/$model:generateContent")
            .header("x-goog-api-key", apiKey)
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }
}
