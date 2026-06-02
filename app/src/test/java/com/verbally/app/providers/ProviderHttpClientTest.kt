package com.verbally.app.providers

import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class ProviderHttpClientTest {
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
    fun defaultOpenAiTranscriptionAndCleanupClientsReuseConnection() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("""{"text":"Hello"}""").build())
        server.enqueue(MockResponse.Builder().code(200).body("""{"output_text":"Hello."}""").build())

        val transcriptionClient = OpenAiTranscriptionClient(
            requestFactory = OpenAiTranscriptionRequestFactory(baseUrl = server.url("/").toString()),
        )
        val cleanupClient = OpenAiTextCleanupClient(
            requestFactory = OpenAiCleanupRequestFactory(baseUrl = server.url("/").toString()),
        )

        transcriptionClient.transcribe(
            apiKey = "openai-test",
            model = "gpt-4o-mini-transcribe",
            audioFile = tempAudio(),
        )
        cleanupClient.clean(
            apiKey = "openai-test",
            model = "gpt-5.4-nano",
            rawTranscript = "Hello",
            cleanupPrompt = "Clean the transcript.",
        )

        val transcriptionRequest = server.takeRequest()
        val cleanupRequest = server.takeRequest()
        assertEquals(transcriptionRequest.connectionIndex, cleanupRequest.connectionIndex)
    }

    private fun tempAudio(): File =
        File.createTempFile("verbally-openai", ".m4a").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
            deleteOnExit()
        }
}
