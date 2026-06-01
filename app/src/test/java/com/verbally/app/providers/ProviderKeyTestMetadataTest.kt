package com.verbally.app.providers

import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.TranscriptionProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class ProviderKeyTestMetadataTest {
    @Test
    fun transcriptionKeyTestTargetUsesProviderMetadata() {
        val endpoints = ProviderKeyTestEndpoints(groqBaseUrl = "https://example.test/openai")
        val target = endpoints.transcriptionTarget(
            AppSettings(
                groqApiKey = "groq-key",
                transcriptionProvider = TranscriptionProvider.GROQ,
                transcriptionModel = "whisper-large-v3-turbo",
            ),
        )

        assertEquals("Groq", target.providerName)
        assertEquals("https://example.test/openai", target.baseUrl)
        assertEquals("/v1/models/whisper-large-v3-turbo", target.path)
        assertEquals("groq-key", target.apiKey)
        assertEquals(ProviderKeyAuthScheme.BEARER, target.authScheme)
    }

    @Test
    fun cleanupKeyTestTargetUsesProviderMetadata() {
        val endpoints = ProviderKeyTestEndpoints(geminiBaseUrl = "https://gemini.example.test")
        val target = endpoints.cleanupTarget(
            AppSettings(
                geminiApiKey = "gemini-key",
                cleanupProvider = CleanupProvider.GEMINI,
                geminiCleanupModel = "gemini-3.1-flash-lite",
            ),
        )

        assertEquals("Gemini", target.providerName)
        assertEquals("https://gemini.example.test", target.baseUrl)
        assertEquals("/v1beta/models/gemini-3.1-flash-lite", target.path)
        assertEquals("gemini-key", target.apiKey)
        assertEquals(ProviderKeyAuthScheme.GOOGLE_API_KEY, target.authScheme)
    }
}
