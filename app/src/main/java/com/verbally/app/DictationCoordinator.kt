package com.verbally.app

import com.verbally.app.audio.TemporaryAudioRecorder
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.history.DictationHistoryRepository
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.insertion.InsertResult
import com.verbally.app.providers.GeminiTextCleanupClient
import com.verbally.app.providers.OpenAiTextCleanupClient
import com.verbally.app.providers.OpenAiTranscriptionClient
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.SettingsRepository
import java.io.File

class DictationCoordinator(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: DictationHistoryRepository,
    private val audioRecorder: TemporaryAudioRecorder,
    private val transcriptionClient: OpenAiTranscriptionClient,
    private val openAiCleanupClient: OpenAiTextCleanupClient,
    private val geminiCleanupClient: GeminiTextCleanupClient,
    private val insertionFactory: () -> ClipboardPasteInserter,
) {
    private var currentRecording: File? = null

    fun startRecording() {
        currentRecording = audioRecorder.start()
    }

    fun cancelRecording() {
        audioRecorder.stopAndDelete()
        currentRecording = null
    }

    fun currentAmplitude(): Int = audioRecorder.currentAmplitude()

    suspend fun confirmRecording(appLabel: String?): InsertResult {
        val audioFile = audioRecorder.stop() ?: currentRecording
        currentRecording = null
        if (audioFile == null) return InsertResult(false, "沒有可處理的錄音。")

        return try {
            val settings = settingsRepository.load()
            val raw = transcriptionClient.transcribe(
                apiKey = settings.openAiApiKey,
                model = settings.transcriptionModel,
                audioFile = audioFile,
            )
            val cleaned = when (settings.cleanupProvider) {
                CleanupProvider.OPENAI -> openAiCleanupClient.clean(
                    apiKey = settings.openAiApiKey,
                    model = settings.openAiCleanupModel,
                    rawTranscript = raw.text,
                    cleanupPrompt = settings.cleanupPrompt,
                )
                CleanupProvider.GEMINI -> geminiCleanupClient.clean(
                    apiKey = settings.geminiApiKey,
                    model = settings.geminiCleanupModel,
                    rawTranscript = raw.text,
                    cleanupPrompt = settings.cleanupPrompt,
                )
            }
            val insertResult = insertionFactory().insert(cleaned.text)
            historyRepository.save(
                DictationHistoryEntry(
                    rawTranscript = raw.text,
                    cleanedText = cleaned.text,
                    createdAtMillis = System.currentTimeMillis(),
                    provider = cleaned.provider,
                    transcriptionModel = raw.model,
                    cleanupModel = cleaned.model,
                    appLabel = appLabel,
                ),
            )
            insertResult
        } finally {
            audioRecorder.delete(audioFile)
        }
    }
}
