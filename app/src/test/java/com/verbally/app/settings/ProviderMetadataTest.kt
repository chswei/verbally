package com.verbally.app.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class ProviderMetadataTest {
    @Test
    fun transcriptionProvidersExposeDisplayNamesAndSelectedApiKey() {
        val settings = AppSettings(
            openAiApiKey = "openai-key",
            sonioxApiKey = "soniox-key",
            groqApiKey = "groq-key",
        )

        assertEquals("OpenAI", TranscriptionProvider.OPENAI.displayName)
        assertEquals("Soniox", TranscriptionProvider.SONIOX.displayName)
        assertEquals("Groq", TranscriptionProvider.GROQ.displayName)
        assertEquals("openai-key", settings.copy(transcriptionProvider = TranscriptionProvider.OPENAI).transcriptionApiKey)
        assertEquals("soniox-key", settings.copy(transcriptionProvider = TranscriptionProvider.SONIOX).transcriptionApiKey)
        assertEquals("groq-key", settings.copy(transcriptionProvider = TranscriptionProvider.GROQ).transcriptionApiKey)
    }

    @Test
    fun cleanupProvidersExposeDisplayNamesAndSelectedApiKey() {
        val settings = AppSettings(
            openAiApiKey = "openai-key",
            geminiApiKey = "gemini-key",
        )

        assertEquals("OpenAI", CleanupProvider.OPENAI.displayName)
        assertEquals("Gemini", CleanupProvider.GEMINI.displayName)
        assertEquals("openai-key", settings.copy(cleanupProvider = CleanupProvider.OPENAI).cleanupApiKey)
        assertEquals("gemini-key", settings.copy(cleanupProvider = CleanupProvider.GEMINI).cleanupApiKey)
    }
}
