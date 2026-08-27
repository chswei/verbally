package com.verbally.app.providers

import java.io.File
import java.util.Base64
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeminiTranscriptionClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun sendsInlineAudioInOneInteractionAndParsesText() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body(
                    """
                    {
                      "id": "interactions/tx-123",
                      "status": "completed",
                      "steps": [
                        {
                          "type": "model_output",
                          "content": [
                            {"type": "text", "text": "今天下午三點開會。"}
                          ]
                        }
                      ]
                    }
                    """.trimIndent(),
                )
                .build(),
        )
        val transcript = client().transcribe(
            apiKey = "gemini-test",
            model = "gemini-3.5-transcribe",
            audioFile = tempAudio(),
        )

        assertEquals("今天下午三點開會。", transcript.text)
        assertEquals("gemini", transcript.provider)
        assertEquals("gemini-3.5-transcribe", transcript.model)

        val interaction = server.takeRequest()
        assertEquals("/v1beta/interactions", interaction.target)
        assertEquals("gemini-test", interaction.headers["x-goog-api-key"])
        val interactionBody = JSONObject(interaction.body!!.utf8())
        assertEquals("gemini-3.5-transcribe", interactionBody.getString("model"))
        val audio = interactionBody.getJSONArray("input").getJSONObject(0)
        assertEquals("audio", audio.getString("type"))
        assertEquals("audio/wav", audio.getString("mime_type"))
        assertEquals(
            Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3, 4)),
            audio.getString("data"),
        )
        assertFalse(audio.has("uri"))
        assertFalse(interactionBody.has("generation_config"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun mapsBlankInteractionTextToNoDictation() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body(
                    """
                    {
                      "status": "completed",
                      "steps": [
                        {"type": "model_output", "content": [{"type": "text", "text": "   "}]}
                      ]
                    }
                    """.trimIndent(),
                )
                .build(),
        )
        val transcript = client().transcribe("gemini-test", "gemini-3.5-transcribe", tempAudio())

        assertEquals("", transcript.text)
        assertEquals(TranscriptionConfidence.NONE, transcript.confidence)
        assertEquals(TranscriptionHallucination.SILENT, transcript.hallucination)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun surfacesInteractionFailureAfterOneRequest() {
        server.enqueue(MockResponse.Builder().code(503).body("unavailable").build())

        val error = assertThrows(ProviderException::class.java) {
            runBlocking {
                client().transcribe("gemini-test", "gemini-3.5-transcribe", tempAudio())
            }
        }

        assertEquals("Gemini 轉錄失敗：503", error.message)
        assertEquals("/v1beta/interactions", server.takeRequest().target)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun reportsMalformedSuccessfulInteractionAfterOneRequest() {
        server.enqueue(MockResponse.Builder().code(200).body("{\"status\":\"completed\"}").build())

        val error = assertThrows(ProviderException::class.java) {
            runBlocking {
                client().transcribe("gemini-test", "gemini-3.5-transcribe", tempAudio())
            }
        }

        assertEquals("Gemini 回傳格式無法解析。", error.message)
        assertEquals("/v1beta/interactions", server.takeRequest().target)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun missingKeyStopsBeforeUpload() {
        val error = assertThrows(ProviderException::class.java) {
            runBlocking {
                client().transcribe("", "gemini-3.5-transcribe", tempAudio())
            }
        }

        assertEquals("請先在設定中填入 Gemini API Key。", error.message)
        assertEquals(0, server.requestCount)
    }

    private fun client() = GeminiTranscriptionClient(
        httpClient = OkHttpClient(),
        baseUrl = server.url("/").toString(),
    )

    private fun tempAudio(): File =
        File.createTempFile("verbally-gemini", ".wav").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
            deleteOnExit()
        }
}
