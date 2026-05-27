package com.verbally.app

import com.verbally.app.audio.AudioRecorder
import com.verbally.app.dictionary.DictionaryRepository
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.history.DictationHistoryRepository
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.insertion.InsertResult
import com.verbally.app.providers.TextCleanupClient
import com.verbally.app.providers.TranscriptionClient
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.SettingsRepository
import com.verbally.app.snippets.SnippetExpander
import com.verbally.app.snippets.SnippetRepository
import java.io.File

class DictationCoordinator(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: DictationHistoryRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val snippetRepository: SnippetRepository,
    private val audioRecorder: AudioRecorder,
    private val transcriptionClient: TranscriptionClient,
    private val openAiCleanupClient: TextCleanupClient,
    private val geminiCleanupClient: TextCleanupClient,
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
            val dictionaryEntries = dictionaryRepository.list()
            val snippetEntries = snippetRepository.list()
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
                    dictionaryEntries = dictionaryEntries,
                )
                CleanupProvider.GEMINI -> geminiCleanupClient.clean(
                    apiKey = settings.geminiApiKey,
                    model = settings.geminiCleanupModel,
                    rawTranscript = raw.text,
                    cleanupPrompt = settings.cleanupPrompt,
                    dictionaryEntries = dictionaryEntries,
                )
            }
            val expandedText = SnippetExpander.expand(cleaned.text, snippetEntries)
            val insertResult = insertionFactory().insert(expandedText)
            historyRepository.save(
                DictationHistoryEntry(
                    rawTranscript = raw.text,
                    cleanedText = expandedText,
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
