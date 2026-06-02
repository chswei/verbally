package com.verbally.app.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File

class OpenAiTranscriptionClient(
    private val httpClient: OkHttpClient = ProviderHttpClients.shared,
    private val requestFactory: OpenAiTranscriptionRequestFactory = OpenAiTranscriptionRequestFactory(),
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey("OpenAI"))
            val response = httpClient.newCall(requestFactory.create(apiKey, model, audioFile)).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException(messages.transcriptionFailed("OpenAI", it.code.toString()))
                val json = JSONObject(body)
                val text = json.optString("text")
                if (text.isBlank()) return@withContext noDictationRawTranscript(model, provider = "openai")
                val averageLogprob = json.averageLogprob()
                RawTranscript(
                    text = text,
                    model = model,
                    provider = "openai",
                    averageLogprob = averageLogprob,
                    confidence = averageLogprob?.let(::confidenceFromAverageLogprob),
                )
            }
        }
}

class GroqTranscriptionClient(
    private val httpClient: OkHttpClient = ProviderHttpClients.shared,
    private val requestFactory: GroqTranscriptionRequestFactory = GroqTranscriptionRequestFactory(),
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey("Groq"))
            val response = httpClient.newCall(requestFactory.create(apiKey, model, audioFile)).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException(messages.transcriptionFailed("Groq", it.code.toString()))
                val text = JSONObject(body).optString("text")
                if (text.isBlank()) {
                    noDictationRawTranscript(model, provider = "groq")
                } else {
                    RawTranscript(text = text, model = model, provider = "groq")
                }
            }
        }
}

private fun JSONObject.averageLogprob(): Double? {
    val logprobs = optJSONArray("logprobs") ?: return null
    var count = 0
    var total = 0.0
    for (index in 0 until logprobs.length()) {
        val token = logprobs.optJSONObject(index) ?: continue
        val logprob = token.optDouble("logprob", Double.NaN)
        if (!logprob.isNaN()) {
            total += logprob
            count += 1
        }
    }
    return if (count == 0) null else total / count
}
