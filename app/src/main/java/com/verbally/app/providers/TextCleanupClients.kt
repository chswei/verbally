package com.verbally.app.providers

import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.style.CleanupStyleContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject

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
    private val httpClient: OkHttpClient = ProviderHttpClients.shared,
    private val requestFactory: OpenAiCleanupRequestFactory = OpenAiCleanupRequestFactory(),
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
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
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey("OpenAI"))
            val response = httpClient.newCall(
                requestFactory.create(apiKey, model, rawTranscript, cleanupPrompt, dictionaryEntries, styleContext),
            ).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException(messages.cleanupFailed("OpenAI", it.code.toString()))
                val json = JSONObject(body)
                val text = json.optString("output_text").ifBlank {
                    json.optJSONArray("output")
                        ?.optJSONObject(0)
                        ?.optJSONArray("content")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        .orEmpty()
                }
                if (text.isBlank()) throw ProviderException(messages.noCleanedText("OpenAI"))
                CleanedTranscript(text = text, provider = "openai", model = model)
            }
        }
}

class GeminiTextCleanupClient(
    private val httpClient: OkHttpClient = ProviderHttpClients.shared,
    private val requestFactory: GeminiCleanupRequestFactory = GeminiCleanupRequestFactory(),
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
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
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey("Gemini"))
            val response = httpClient.newCall(
                requestFactory.create(apiKey, model, rawTranscript, cleanupPrompt, dictionaryEntries, styleContext),
            ).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException(messages.cleanupFailed("Gemini", it.code.toString()))
                val text = JSONObject(body)
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    .orEmpty()
                if (text.isBlank()) throw ProviderException(messages.noCleanedText("Gemini"))
                CleanedTranscript(text = text, provider = "gemini", model = model)
            }
        }
}
