package com.verbally.app.providers

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
                RawTranscript(text = text, model = model, provider = "openai")
            }
        }
}

class GroqTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val requestFactory: GroqTranscriptionRequestFactory = GroqTranscriptionRequestFactory(),
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException("請先在設定中填入 Groq API Key。")
            val response = httpClient.newCall(requestFactory.create(apiKey, model, audioFile)).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException("Groq 轉錄失敗：${it.code}")
                val text = JSONObject(body).optString("text")
                if (text.isBlank()) throw ProviderException("Groq 沒有回傳轉錄文字。")
                RawTranscript(text = text, model = model, provider = "groq")
            }
        }
}

class DeepgramTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val requestFactory: DeepgramTranscriptionRequestFactory = DeepgramTranscriptionRequestFactory(),
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException("請先在設定中填入 Deepgram API Key。")
            val response = httpClient.newCall(requestFactory.create(apiKey, model, audioFile)).execute()
            response.use {
                val body = it.body.string()
                if (!it.isSuccessful) throw ProviderException("Deepgram 轉錄失敗：${it.code}")
                val text = JSONObject(body)
                    .optJSONObject("results")
                    ?.optJSONArray("channels")
                    ?.optJSONObject(0)
                    ?.optJSONArray("alternatives")
                    ?.optJSONObject(0)
                    ?.optString("transcript")
                    .orEmpty()
                if (text.isBlank()) throw ProviderException("Deepgram 沒有回傳轉錄文字。")
                RawTranscript(text = text, model = model, provider = "deepgram")
            }
        }
}

class SonioxRealtimeTranscriptionClient(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val configFactory: SonioxRealtimeConfigFactory = SonioxRealtimeConfigFactory(),
    private val websocketUrl: String = "wss://stt-rt.soniox.com/transcribe-websocket",
) : TranscriptionClient {
    override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) throw ProviderException("請先在設定中填入 Soniox API Key。")
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
                    continuation.resumeWithException(ProviderException("Soniox 沒有回傳轉錄文字。"))
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
                        fail(ProviderException("Soniox 音訊串流失敗：${it.message.orEmpty()}"), webSocket)
                    }
                }.start()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val json = runCatching { JSONObject(text) }.getOrElse {
                    fail(ProviderException("Soniox 回傳格式無法解析。"), webSocket)
                    return
                }
                val errorCode = json.optString("error_code")
                if (errorCode.isNotBlank()) {
                    fail(
                        ProviderException(
                            "Soniox 轉錄失敗：$errorCode ${json.optString("error_message")}".trim(),
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
                fail(ProviderException("Soniox 轉錄失敗：${t.message.orEmpty()}"), webSocket)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!completed.get()) {
                    fail(ProviderException("Soniox 連線已關閉。"), webSocket)
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
