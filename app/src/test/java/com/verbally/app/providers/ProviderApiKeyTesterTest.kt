package com.verbally.app.providers

import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.TranscriptionProvider
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ProviderApiKeyTesterTest {
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
    fun testsOpenAiTranscriptionKeyWithModelRequest() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())
        val tester = ProviderApiKeyTester(
            endpoints = ProviderKeyTestEndpoints(openAiBaseUrl = server.url("/").toString()),
        )

        val result = tester.testTranscription(
            AppSettings(
                openAiApiKey = "sk-test",
                transcriptionProvider = TranscriptionProvider.OPENAI,
                transcriptionModel = "gpt-4o-mini-transcribe",
            ),
        )

        assertEquals(ProviderKeyTestResult.Success("OpenAI"), result)
        val request = server.takeRequest()
        assertEquals("/v1/models/gpt-4o-mini-transcribe", request.target)
        assertEquals("Bearer sk-test", request.headers["Authorization"])
    }

    @Test
    fun testsGroqTranscriptionKeyWithOpenAiCompatibleModelRequest() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())
        val tester = ProviderApiKeyTester(
            endpoints = ProviderKeyTestEndpoints(groqBaseUrl = server.url("/openai").toString()),
        )

        val result = tester.testTranscription(
            AppSettings(
                groqApiKey = "groq-test",
                transcriptionProvider = TranscriptionProvider.GROQ,
                transcriptionModel = "whisper-large-v3-turbo",
            ),
        )

        assertEquals(ProviderKeyTestResult.Success("Groq"), result)
        val request = server.takeRequest()
        assertEquals("/openai/v1/models/whisper-large-v3-turbo", request.target)
        assertEquals("Bearer groq-test", request.headers["Authorization"])
    }

    @Test
    fun testsSonioxTranscriptionKeyWithModelsRequest() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())
        val tester = ProviderApiKeyTester(
            endpoints = ProviderKeyTestEndpoints(sonioxBaseUrl = server.url("/").toString()),
        )

        val result = tester.testTranscription(
            AppSettings(
                sonioxApiKey = "soniox-test",
                transcriptionProvider = TranscriptionProvider.SONIOX,
            ),
        )

        assertEquals(ProviderKeyTestResult.Success("Soniox"), result)
        val request = server.takeRequest()
        assertEquals("/v1/models", request.target)
        assertEquals("Bearer soniox-test", request.headers["Authorization"])
    }

    @Test
    fun testsOpenAiCleanupKeyWithModelRequest() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())
        val tester = ProviderApiKeyTester(
            endpoints = ProviderKeyTestEndpoints(openAiBaseUrl = server.url("/").toString()),
        )

        val result = tester.testCleanup(
            AppSettings(
                cleanupProvider = CleanupProvider.OPENAI,
                openAiApiKey = "sk-cleanup",
                openAiCleanupModel = "gpt-4.1-mini",
            ),
        )

        assertEquals(ProviderKeyTestResult.Success("OpenAI"), result)
        val request = server.takeRequest()
        assertEquals("/v1/models/gpt-4.1-mini", request.target)
        assertEquals("Bearer sk-cleanup", request.headers["Authorization"])
    }

    @Test
    fun testsGeminiCleanupKeyWithModelRequest() = runBlocking {
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())
        val tester = ProviderApiKeyTester(
            endpoints = ProviderKeyTestEndpoints(geminiBaseUrl = server.url("/").toString()),
        )

        val result = tester.testCleanup(
            AppSettings(
                cleanupProvider = CleanupProvider.GEMINI,
                geminiApiKey = "gemini-test",
                geminiCleanupModel = "gemini-3.1-flash-lite",
            ),
        )

        assertEquals(ProviderKeyTestResult.Success("Gemini"), result)
        val request = server.takeRequest()
        assertEquals("/v1beta/models/gemini-3.1-flash-lite", request.target)
        assertEquals("gemini-test", request.headers["x-goog-api-key"])
    }

    @Test
    fun returnsMissingKeyWithoutNetworkRequest() = runBlocking {
        val tester = ProviderApiKeyTester(
            endpoints = ProviderKeyTestEndpoints(openAiBaseUrl = server.url("/").toString()),
        )

        val result = tester.testTranscription(AppSettings(openAiApiKey = ""))

        assertEquals(ProviderKeyTestResult.MissingKey("OpenAI"), result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun returnsFailureForProviderRejection() = runBlocking {
        server.enqueue(MockResponse.Builder().code(401).body("invalid key").build())
        val tester = ProviderApiKeyTester(
            endpoints = ProviderKeyTestEndpoints(openAiBaseUrl = server.url("/").toString()),
        )

        val result = tester.testCleanup(AppSettings(openAiApiKey = "bad-key"))

        assertEquals(ProviderKeyTestResult.Failure("OpenAI", "HTTP 401"), result)
    }
}
