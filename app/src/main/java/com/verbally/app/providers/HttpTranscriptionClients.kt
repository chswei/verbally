package com.verbally.app.providers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File

class OpenAiTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
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
                val text = JSONObject(body).optString("text")
                RawTranscript(text = text, model = model, provider = "openai")
            }
        }
}

class GroqTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
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
                RawTranscript(text = text, model = model, provider = "groq")
            }
        }
}

class DeepgramTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val requestFactory: DeepgramTranscriptionRequestFactory = DeepgramTranscriptionRequestFactory(),
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey("Deepgram"))
            val response = httpClient.newCall(requestFactory.create(apiKey, model, audioFile)).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException(messages.transcriptionFailed("Deepgram", it.code.toString()))
                val text = JSONObject(body)
                    .optJSONObject("results")
                    ?.optJSONArray("channels")
                    ?.optJSONObject(0)
                    ?.optJSONArray("alternatives")
                    ?.optJSONObject(0)
                    ?.optString("transcript")
                    .orEmpty()
                RawTranscript(text = text, model = model, provider = "deepgram")
            }
        }
}
