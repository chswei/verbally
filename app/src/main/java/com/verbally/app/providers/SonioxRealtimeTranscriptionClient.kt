package com.verbally.app.providers

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
                continuation.resume(text)
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
