package com.verbally.app.providers

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class SonioxAsyncTranscriptionClientTest {
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
    fun transcribesRecordedAudioWithAsyncRestWorkflow() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"file-123","filename":"clip.m4a","size":4,"created_at":"2026-06-02T00:00:00Z"}""")
                .build(),
        )
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"tx-123","status":"queued","model":"stt-async-v4","filename":"clip.m4a"}""")
                .build(),
        )
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","status":"processing"}""").build())
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","status":"completed"}""").build())
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","text":"你好 Soniox","tokens":[]}""").build())
        server.enqueue(MockResponse.Builder().code(204).build())

        val audio = tempAudio()
        val client = SonioxAsyncTranscriptionClient(
            httpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
            pollDelayMillis = 0,
        )

        val transcript = client.transcribe(
            apiKey = "soniox-test",
            model = "stt-async-v4",
            audioFile = audio,
        )

        assertEquals(RawTranscript(text = "你好 Soniox", model = "stt-async-v4", provider = "soniox"), transcript)

        val upload = server.takeRequest()
        assertEquals("POST", upload.method)
        assertEquals("/v1/files", upload.target)
        assertEquals("Bearer soniox-test", upload.headers["Authorization"])
        assertTrue(upload.body!!.utf8().contains("name=\"file\""))

        val create = server.takeRequest()
        assertEquals("POST", create.method)
        assertEquals("/v1/transcriptions", create.target)
        val createBody = create.body!!.utf8()
        assertTrue(createBody.contains("\"model\":\"stt-async-v4\""))
        assertTrue(createBody.contains("\"file_id\":\"file-123\""))
        assertTrue(createBody.contains("\"enable_language_identification\":true"))

        assertEquals("/v1/transcriptions/tx-123", server.takeRequest().target)
        assertEquals("/v1/transcriptions/tx-123", server.takeRequest().target)
        assertEquals("/v1/transcriptions/tx-123/transcript", server.takeRequest().target)

        val delete = server.takeRequest(1, TimeUnit.SECONDS)
            ?: throw AssertionError("Expected background Soniox transcription cleanup request")
        assertEquals("DELETE", delete.method)
        assertEquals("/v1/transcriptions/tx-123", delete.target)
    }

    @Test
    fun blankTranscriptReturnsSilentNoContentResult() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"file-123","filename":"clip.m4a","size":4,"created_at":"2026-06-02T00:00:00Z"}""")
                .build(),
        )
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"tx-123","status":"queued","model":"stt-async-v4","filename":"clip.m4a"}""")
                .build(),
        )
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","status":"completed"}""").build())
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","text":"","tokens":[]}""").build())
        server.enqueue(MockResponse.Builder().code(204).build())

        val client = SonioxAsyncTranscriptionClient(
            httpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
            pollDelayMillis = 0,
        )

        val transcript = client.transcribe(
            apiKey = "soniox-test",
            model = "stt-async-v4",
            audioFile = tempAudio(),
        )

        assertEquals("", transcript.text)
        assertEquals(TranscriptionConfidence.NONE, transcript.confidence)
        assertEquals(TranscriptionHallucination.SILENT, transcript.hallucination)
    }

    @Test
    fun transcriptIncludesAverageTokenConfidence() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"file-123","filename":"clip.wav","size":4,"created_at":"2026-06-02T00:00:00Z"}""")
                .build(),
        )
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"tx-123","status":"queued","model":"stt-async-v4","filename":"clip.wav"}""")
                .build(),
        )
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","status":"completed"}""").build())
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .body(
                    """
                    {
                      "id": "tx-123",
                      "text": "Thanks for watching",
                      "tokens": [
                        {"text": "Thanks", "confidence": 0.32},
                        {"text": " for", "confidence": 0.41},
                        {"text": " watching", "confidence": 0.48}
                      ]
                    }
                    """.trimIndent(),
                )
                .build(),
        )
        server.enqueue(MockResponse.Builder().code(204).build())

        val client = SonioxAsyncTranscriptionClient(
            httpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
            pollDelayMillis = 0,
        )

        val transcript = client.transcribe(
            apiKey = "soniox-test",
            model = "stt-async-v4",
            audioFile = tempAudio(),
        )

        assertEquals("Thanks for watching", transcript.text)
        assertEquals(TranscriptionConfidence.LOW, transcript.confidence)
    }

    @Test
    fun defaultPollingChecksShortDictationsWithoutWaitingAFullSecond() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"file-123","filename":"clip.m4a","size":4,"created_at":"2026-06-02T00:00:00Z"}""")
                .build(),
        )
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"tx-123","status":"queued","model":"stt-async-v4","filename":"clip.m4a"}""")
                .build(),
        )
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","status":"processing"}""").build())
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","status":"completed"}""").build())
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","text":"你好 Soniox","tokens":[]}""").build())
        server.enqueue(MockResponse.Builder().code(204).build())

        val client = SonioxAsyncTranscriptionClient(
            httpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
        )

        val elapsedMillis = measureTimeMillis {
            client.transcribe(
                apiKey = "soniox-test",
                model = "stt-async-v4",
                audioFile = tempAudio(),
            )
        }

        assertTrue(
            "Expected Soniox default polling to return without a full-second wait, elapsed=${elapsedMillis}ms",
            elapsedMillis < 900,
        )
    }

    @Test
    fun returnsTranscriptBeforeSuccessfulRemoteCleanupFinishes() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"file-123","filename":"clip.m4a","size":4,"created_at":"2026-06-02T00:00:00Z"}""")
                .build(),
        )
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"tx-123","status":"queued","model":"stt-async-v4","filename":"clip.m4a"}""")
                .build(),
        )
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","status":"completed"}""").build())
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","text":"你好 Soniox","tokens":[]}""").build())
        server.enqueue(
            MockResponse.Builder()
                .code(204)
                .headersDelay(1, TimeUnit.SECONDS)
                .build(),
        )

        val client = SonioxAsyncTranscriptionClient(
            httpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
            pollDelayMillis = 0,
        )

        val elapsedMillis = measureTimeMillis {
            val transcript = client.transcribe(
                apiKey = "soniox-test",
                model = "stt-async-v4",
                audioFile = tempAudio(),
            )

            assertEquals(RawTranscript(text = "你好 Soniox", model = "stt-async-v4", provider = "soniox"), transcript)
        }

        assertTrue(
            "Expected transcript to return before remote cleanup response finishes, elapsed=${elapsedMillis}ms",
            elapsedMillis < 700,
        )

        repeat(4) { server.takeRequest() }
        val delete = server.takeRequest(1, TimeUnit.SECONDS)
            ?: throw AssertionError("Expected background Soniox transcription cleanup request")
        assertEquals("DELETE", delete.method)
        assertEquals("/v1/transcriptions/tx-123", delete.target)
    }

    @Test
    fun deletesUploadedFileWhenProcessingTranscriptionCannotBeDeletedAfterTimeout() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"file-123","filename":"clip.m4a","size":4,"created_at":"2026-06-02T00:00:00Z"}""")
                .build(),
        )
        server.enqueue(
            MockResponse.Builder()
                .code(201)
                .body("""{"id":"tx-123","status":"queued","model":"stt-async-v4","filename":"clip.m4a"}""")
                .build(),
        )
        server.enqueue(MockResponse.Builder().code(200).body("""{"id":"tx-123","status":"processing"}""").build())
        server.enqueue(MockResponse.Builder().code(409).body("""{"message":"still processing"}""").build())
        server.enqueue(MockResponse.Builder().code(204).build())

        val client = SonioxAsyncTranscriptionClient(
            httpClient = OkHttpClient(),
            baseUrl = server.url("/").toString(),
            pollDelayMillis = 0,
            maxPollAttempts = 1,
        )

        val error = assertThrows(ProviderException::class.java) {
            runBlocking {
                client.transcribe(
                    apiKey = "soniox-test",
                    model = "stt-async-v4",
                    audioFile = tempAudio(),
                )
            }
        }

        assertEquals("Soniox 轉錄失敗：timeout", error.message)
        server.takeRequest()
        server.takeRequest()
        server.takeRequest()
        val transcriptionDelete = server.takeRequest()
        assertEquals("DELETE", transcriptionDelete.method)
        assertEquals("/v1/transcriptions/tx-123", transcriptionDelete.target)
        val fileDelete = server.takeRequest(1, TimeUnit.SECONDS)
            ?: throw AssertionError("Expected Soniox file cleanup request")
        assertEquals("DELETE", fileDelete.method)
        assertEquals("/v1/files/file-123", fileDelete.target)
    }

    private fun tempAudio(): File =
        File.createTempFile("verbally-soniox", ".m4a").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
            deleteOnExit()
        }
}
