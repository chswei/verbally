package com.verbally.app.providers

import android.content.Context
import com.verbally.app.R
import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.TranscriptionProvider
import com.verbally.app.style.CleanupStyleContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class RawTranscript(
    val text: String,
    val model: String,
    val provider: String = "openai",
)

data class CleanedTranscript(
    val text: String,
    val provider: String,
    val model: String,
)

class ProviderException(message: String) : RuntimeException(message)

interface ProviderMessages {
    fun missingApiKey(provider: String): String
    fun transcriptionFailed(provider: String, detail: String): String
    fun noTranscriptionText(provider: String): String
    fun audioStreamFailed(detail: String): String
    fun responseParseFailed(provider: String): String
    fun connectionClosed(provider: String): String
    fun cleanupFailed(provider: String, detail: String): String
    fun noCleanedText(provider: String): String

    companion object {
        val TraditionalChinese: ProviderMessages = object : ProviderMessages {
            override fun missingApiKey(provider: String): String = "請先在設定中填入 $provider API Key。"
            override fun transcriptionFailed(provider: String, detail: String): String = "$provider 轉錄失敗：$detail"
            override fun noTranscriptionText(provider: String): String = "$provider 沒有回傳轉錄文字。"
            override fun audioStreamFailed(detail: String): String = "Soniox 音訊串流失敗：$detail"
            override fun responseParseFailed(provider: String): String = "$provider 回傳格式無法解析。"
            override fun connectionClosed(provider: String): String = "$provider 連線已關閉。"
            override fun cleanupFailed(provider: String, detail: String): String = "$provider 文字整理失敗：$detail"
            override fun noCleanedText(provider: String): String = "$provider 沒有回傳整理後文字。"
        }
    }
}

class AndroidProviderMessages(private val context: Context) : ProviderMessages {
    override fun missingApiKey(provider: String): String =
        context.getString(R.string.provider_missing_api_key, provider)

    override fun transcriptionFailed(provider: String, detail: String): String =
        context.getString(R.string.provider_transcription_failed, provider, detail)

    override fun noTranscriptionText(provider: String): String =
        context.getString(R.string.provider_no_transcription_text, provider)

    override fun audioStreamFailed(detail: String): String =
        context.getString(R.string.provider_audio_stream_failed, detail)

    override fun responseParseFailed(provider: String): String =
        context.getString(R.string.provider_response_parse_failed, provider)

    override fun connectionClosed(provider: String): String =
        context.getString(R.string.provider_connection_closed, provider)

    override fun cleanupFailed(provider: String, detail: String): String =
        context.getString(R.string.provider_cleanup_failed, provider, detail)

    override fun noCleanedText(provider: String): String =
        context.getString(R.string.provider_no_cleaned_text, provider)
}

interface TranscriptionClient {
    suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript
}

class TranscriptionClientRouter(
    private val openAiClient: TranscriptionClient,
    private val sonioxClient: TranscriptionClient,
    private val groqClient: TranscriptionClient,
    private val deepgramClient: TranscriptionClient,
) {
    suspend fun transcribe(settings: AppSettings, audioFile: File): RawTranscript {
        val client = when (settings.transcriptionProvider) {
            TranscriptionProvider.OPENAI -> openAiClient
            TranscriptionProvider.SONIOX -> sonioxClient
            TranscriptionProvider.GROQ -> groqClient
            TranscriptionProvider.DEEPGRAM -> deepgramClient
        }
        return client.transcribe(
            apiKey = settings.transcriptionApiKey,
            model = settings.transcriptionModel,
            audioFile = audioFile,
        )
    }

    private val AppSettings.transcriptionApiKey: String
        get() = when (transcriptionProvider) {
            TranscriptionProvider.OPENAI -> openAiApiKey
            TranscriptionProvider.SONIOX -> sonioxApiKey
            TranscriptionProvider.GROQ -> groqApiKey
            TranscriptionProvider.DEEPGRAM -> deepgramApiKey
        }

    companion object {
        fun single(client: TranscriptionClient): TranscriptionClientRouter =
            TranscriptionClientRouter(
                openAiClient = client,
                sonioxClient = client,
                groqClient = client,
                deepgramClient = client,
            )
    }
}

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
                if (text.isBlank()) throw ProviderException(messages.noTranscriptionText("OpenAI"))
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
                if (text.isBlank()) throw ProviderException(messages.noTranscriptionText("Groq"))
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
                if (text.isBlank()) throw ProviderException(messages.noTranscriptionText("Deepgram"))
                RawTranscript(text = text, model = model, provider = "deepgram")
            }
        }
}

class SonioxRealtimeTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val configFactory: SonioxRealtimeConfigFactory = SonioxRealtimeConfigFactory(),
    private val websocketUrl: String = "wss://stt-rt.soniox.com/transcribe-websocket",
    private val messages: ProviderMessages = ProviderMessages.TraditionalChinese,
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException(messages.missingApiKey("Soniox"))
            val text = transcribeOverWebSocket(apiKey, model, audioFile)
            RawTranscript(text = text, model = model, provider = "soniox")
        }

    private suspend fun transcribeOverWebSocket(
        apiKey: String,
        model: String,
        audioFile: File,
    ): String = suspendCancellableCoroutine { continuation ->
        val completed = AtomicBoolean(false)
        val finalText = StringBuilder()

        fun complete(text: String, webSocket: WebSocket) {
            if (completed.compareAndSet(false, true)) {
                webSocket.close(1000, null)
                if (text.isBlank()) {
                    continuation.resumeWithException(ProviderException(messages.noTranscriptionText("Soniox")))
                } else {
                    continuation.resume(text)
                }
            }
        }

        fun fail(error: Throwable, webSocket: WebSocket? = null) {
            if (completed.compareAndSet(false, true)) {
                webSocket?.cancel()
                continuation.resumeWithException(error)
            }
        }

        val request = Request.Builder().url(websocketUrl).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(configFactory.create(apiKey, model))
                Thread {
                    runCatching {
                        audioFile.inputStream().use { input ->
                            val buffer = ByteArray(3840)
                            while (true) {
                                val count = input.read(buffer)
                                if (count == -1) break
                                webSocket.send(buffer.copyOf(count).toByteString())
                                Thread.sleep(120)
                            }
                        }
                        webSocket.send("")
                    }.onFailure {
                        fail(ProviderException(messages.audioStreamFailed(it.message.orEmpty())), webSocket)
                    }
                }.start()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = runCatching { JSONObject(text) }.getOrElse {
                    fail(ProviderException(messages.responseParseFailed("Soniox")), webSocket)
                    return
                }
                val errorCode = json.optString("error_code")
                if (errorCode.isNotBlank()) {
                    fail(
                        ProviderException(
                            messages.transcriptionFailed(
                                "Soniox",
                                "$errorCode ${json.optString("error_message")}".trim(),
                            ),
                        ),
                        webSocket,
                    )
                    return
                }
                val tokens = json.optJSONArray("tokens")
                if (tokens != null) {
                    for (index in 0 until tokens.length()) {
                        val token = tokens.optJSONObject(index) ?: continue
                        if (token.optBoolean("is_final")) {
                            finalText.append(token.optString("text"))
                        }
                    }
                }
                if (json.optBoolean("finished")) {
                    complete(finalText.toString().trim(), webSocket)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                fail(ProviderException(messages.transcriptionFailed("Soniox", t.message.orEmpty())), webSocket)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!completed.get()) {
                    fail(ProviderException(messages.connectionClosed("Soniox")), webSocket)
                }
            }
        }
        val webSocket = httpClient.newWebSocket(request, listener)
        continuation.invokeOnCancellation { webSocket.cancel() }
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
    private val httpClient: OkHttpClient = OkHttpClient(),
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
