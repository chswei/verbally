package com.verbally.app.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelOptionsTest {
    @Test
    fun transcriptionOptionsExposeFourCuratedChoices() {
        assertEquals(
            listOf(
                "OpenAI: gpt-4o-mini-transcribe",
                "OpenAI: gpt-4o-transcribe",
                "Soniox: stt-async-v4",
                "Groq: whisper-large-v3-turbo",
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
    fun appliesSelectedOptionLabelsToSettings() {
        val transcription = AppSettings().withTranscriptionModelOption("Soniox: stt-async-v4")
        val cleanup = AppSettings().withCleanupModelOption("Gemini: gemini-3.1-pro-preview")

        assertEquals(TranscriptionProvider.SONIOX, transcription.transcriptionProvider)
        assertEquals("stt-async-v4", transcription.transcriptionModel)
        assertEquals(CleanupProvider.GEMINI, cleanup.cleanupProvider)
        assertEquals("gemini-3.1-pro-preview", cleanup.geminiCleanupModel)
        assertTrue(cleanup.cleanupModelOptionLabel.endsWith("gemini-3.1-pro-preview"))
    }

    @Test
    fun fallsBackToSelectedProviderDefaultWhenStoredTranscriptionModelIsStale() {
        val settings = AppSettings(
            transcriptionProvider = TranscriptionProvider.SONIOX,
            transcriptionModel = "stt-rt-v4",
        )

        assertEquals("Soniox: stt-async-v4", settings.transcriptionModelOptionLabel)
        assertEquals("stt-async-v4", settings.transcriptionModelForRequest)
    }
}
