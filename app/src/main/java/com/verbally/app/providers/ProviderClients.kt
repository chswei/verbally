package com.verbally.app.providers

import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.style.CleanupStyleContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File

data class RawTranscript(
    val text: String,
    val model: String,
)

data class CleanedTranscript(
    val text: String,
    val provider: String,
    val model: String,
)

class ProviderException(message: String) : RuntimeException(message)

interface TranscriptionClient {
    suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript
}

class OpenAiTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val requestFactory: OpenAiTranscriptionRequestFactory = OpenAiTranscriptionRequestFactory(),
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException("請先在設定中填入 OpenAI API Key。")
            val response = httpClient.newCall(requestFactory.create(apiKey, model, audioFile)).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException("OpenAI 轉錄失敗：${it.code}")
                val text = JSONObject(body).optString("text")
                if (text.isBlank()) throw ProviderException("OpenAI 沒有回傳轉錄文字。")
                RawTranscript(text = text, model = model)
            }
        }
}

interface TextCleanupClient {
    suspend fun clean(
        apiKey: String,
        model: String,
        rawTranscript: String,
        cleanupPrompt: String,
        dictionaryEntries: List<DictionaryEntry> = emptyList(),
        styleContext: CleanupStyleContext = CleanupStyleContext.default(),
    ): CleanedTranscript
}

class OpenAiTextCleanupClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val requestFactory: OpenAiCleanupRequestFactory = OpenAiCleanupRequestFactory(),
) : TextCleanupClient {
    override suspend fun clean(
        apiKey: String,
        model: String,
        rawTranscript: String,
        cleanupPrompt: String,
        dictionaryEntries: List<DictionaryEntry>,
        styleContext: CleanupStyleContext,
    ): CleanedTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException("請先在設定中填入 OpenAI API Key。")
            val response = httpClient.newCall(
                requestFactory.create(apiKey, model, rawTranscript, cleanupPrompt, dictionaryEntries, styleContext),
            ).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException("OpenAI 文字整理失敗：${it.code}")
                val json = JSONObject(body)
                val text = json.optString("output_text").ifBlank {
                    json.optJSONArray("output")
                        ?.optJSONObject(0)
                        ?.optJSONArray("content")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        .orEmpty()
                }
                if (text.isBlank()) throw ProviderException("OpenAI 沒有回傳整理後文字。")
                CleanedTranscript(text = text, provider = "openai", model = model)
            }
        }
}

class GeminiTextCleanupClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val requestFactory: GeminiCleanupRequestFactory = GeminiCleanupRequestFactory(),
) : TextCleanupClient {
    override suspend fun clean(
        apiKey: String,
        model: String,
        rawTranscript: String,
        cleanupPrompt: String,
        dictionaryEntries: List<DictionaryEntry>,
        styleContext: CleanupStyleContext,
    ): CleanedTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException("請先在設定中填入 Gemini API Key。")
            val response = httpClient.newCall(
                requestFactory.create(apiKey, model, rawTranscript, cleanupPrompt, dictionaryEntries, styleContext),
            ).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException("Gemini 文字整理失敗：${it.code}")
                val text = JSONObject(body)
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    .orEmpty()
                if (text.isBlank()) throw ProviderException("Gemini 沒有回傳整理後文字。")
                CleanedTranscript(text = text, provider = "gemini", model = model)
            }
        }
}
