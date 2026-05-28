package com.verbally.app.providers

import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.style.CleanupStyleContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class OpenAiTranscriptionRequestFactory(
    private val baseUrl: String = "https://api.openai.com",
) {
    fun create(
        apiKey: String,
        model: String,
        audioFile: File,
    ): Request {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", model)
            .addFormDataPart(
                name = "file",
                filename = audioFile.name,
                body = audioFile.asRequestBody(mediaTypeFor(audioFile).toMediaType()),
            )
            .addFormDataPart("response_format", "json")
            .build()

        return Request.Builder()
            .url("${baseUrl.trimEnd('/')}/v1/audio/transcriptions")
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()
    }
}

class OpenAiCleanupRequestFactory(
    private val baseUrl: String = "https://api.openai.com",
) {
    fun create(
        apiKey: String,
        model: String,
        rawTranscript: String,
        cleanupPrompt: String = CleanupPromptFactory.defaultCleanupPrompt,
        dictionaryEntries: List<DictionaryEntry> = emptyList(),
        styleContext: CleanupStyleContext? = null,
    ): Request {
        val json = """
            {
              "model": "${escapeJson(model)}",
              "input": ${
                  jsonString(
                      CleanupPromptFactory.cleanupPrompt(
                          cleanupPrompt,
                          rawTranscript,
                          dictionaryEntries,
                          styleContext,
                      ),
                  )
              }
            }
        """.trimIndent()

        return Request.Builder()
            .url("${baseUrl.trimEnd('/')}/v1/responses")
            .header("Authorization", "Bearer $apiKey")
            .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
    }
}

internal fun jsonString(value: String): String = buildString {
    append('"')
    value.forEach { char ->
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
    append('"')
}

internal fun escapeJson(value: String): String = jsonString(value).removeSurrounding("\"")

private fun mediaTypeFor(file: File): String = when (file.extension.lowercase()) {
    "m4a" -> "audio/mp4"
    "mp3" -> "audio/mpeg"
    "webm" -> "audio/webm"
    "ogg" -> "audio/ogg"
    else -> "audio/wav"
}
