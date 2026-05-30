package com.verbally.app.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelOptionsTest {
    @Test
    fun transcriptionOptionsExposeFiveCuratedChoices() {
        assertEquals(
            listOf(
                "OpenAI: gpt-4o-mini-transcribe",
                "OpenAI: gpt-4o-transcribe",
                "Soniox: Soniox Realtime",
                "Groq: whisper-large-v3-turbo",
                "Deepgram: Real-time Nova-3",
            ),
            ModelOptions.TranscriptionOptions.map { it.label },
        )
    }

    @Test
    fun cleanupOptionsExposeFiveCuratedChoices() {
        assertEquals(
            listOf(
                "OpenAI: gpt-5.4-nano",
                "OpenAI: gpt-5.4-mini",
                "OpenAI: gpt-5.5",
                "Gemini: gemini-3.1-flash-lite",
                "Gemini: gemini-3.1-pro-preview",
            ),
            ModelOptions.CleanupOptions.map { it.label },
        )
    }

    @Test
    fun defaultsUseRecommendedLowCostModels() {
        val settings = AppSettings()

        assertEquals(TranscriptionProvider.OPENAI, settings.transcriptionProvider)
        assertEquals("gpt-4o-mini-transcribe", settings.transcriptionModel)
        assertEquals(CleanupProvider.OPENAI, settings.cleanupProvider)
        assertEquals("gpt-5.4-nano", settings.openAiCleanupModel)
    }

    @Test
    fun normalizesUnsupportedModelsToSupportedDefaults() {
        val settings = AppSettings(
            transcriptionProvider = TranscriptionProvider.DEEPGRAM,
            transcriptionModel = "not-a-real-model",
            openAiCleanupModel = "old-openai-model",
            geminiCleanupModel = "old-gemini-model",
        ).normalizedModelChoices()

        assertEquals(TranscriptionProvider.OPENAI, settings.transcriptionProvider)
        assertEquals("gpt-4o-mini-transcribe", settings.transcriptionModel)
        assertEquals("gpt-5.4-nano", settings.openAiCleanupModel)
        assertEquals("gemini-3.1-flash-lite", settings.geminiCleanupModel)
    }

    @Test
    fun appliesSelectedOptionLabelsToSettings() {
        val transcription = AppSettings().withTranscriptionModelOption("Groq: whisper-large-v3-turbo")
        val cleanup = AppSettings().withCleanupModelOption("Gemini: gemini-3.1-pro-preview")

        assertEquals(TranscriptionProvider.GROQ, transcription.transcriptionProvider)
        assertEquals("whisper-large-v3-turbo", transcription.transcriptionModel)
        assertEquals(CleanupProvider.GEMINI, cleanup.cleanupProvider)
        assertEquals("gemini-3.1-pro-preview", cleanup.geminiCleanupModel)
        assertTrue(cleanup.cleanupModelOptionLabel.endsWith("gemini-3.1-pro-preview"))
    }
}
