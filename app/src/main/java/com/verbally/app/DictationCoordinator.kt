package com.verbally.app

import com.verbally.app.audio.AudioRecorder
import com.verbally.app.dictionary.DictionaryRepository
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.history.DictationHistoryRepository
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.insertion.InsertResult
import com.verbally.app.providers.TextCleanupClient
import com.verbally.app.providers.TranscriptionClient
import com.verbally.app.providers.TranscriptionClientRouter
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.SettingsRepository
import com.verbally.app.snippets.SnippetExpander
import com.verbally.app.snippets.SnippetRepository
import com.verbally.app.style.AppCategoryClassifier
import com.verbally.app.style.AppStyleProfileRepository
import com.verbally.app.style.CleanupStyleContext
import java.io.File

class DictationCoordinator(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: DictationHistoryRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val snippetRepository: SnippetRepository,
    private val styleProfileRepository: AppStyleProfileRepository,
    private val audioRecorder: AudioRecorder,
    private val transcriptionRouter: TranscriptionClientRouter,
    private val openAiCleanupClient: TextCleanupClient,
    private val geminiCleanupClient: TextCleanupClient,
    private val insertionFactory: () -> ClipboardPasteInserter,
) {
    constructor(
        settingsRepository: SettingsRepository,
        historyRepository: DictationHistoryRepository,
        dictionaryRepository: DictionaryRepository,
        snippetRepository: SnippetRepository,
        styleProfileRepository: AppStyleProfileRepository,
        audioRecorder: AudioRecorder,
        transcriptionClient: TranscriptionClient,
        openAiCleanupClient: TextCleanupClient,
        geminiCleanupClient: TextCleanupClient,
        insertionFactory: () -> ClipboardPasteInserter,
    ) : this(
        settingsRepository = settingsRepository,
        historyRepository = historyRepository,
        dictionaryRepository = dictionaryRepository,
        snippetRepository = snippetRepository,
        styleProfileRepository = styleProfileRepository,
        audioRecorder = audioRecorder,
        transcriptionRouter = TranscriptionClientRouter.single(transcriptionClient),
        openAiCleanupClient = openAiCleanupClient,
        geminiCleanupClient = geminiCleanupClient,
        insertionFactory = insertionFactory,
    )

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
            val appCategory = AppCategoryClassifier.classify(appLabel)
            val styleContext = CleanupStyleContext(
                category = appCategory,
                style = styleProfileRepository.styleFor(appCategory),
            )
            val raw = transcriptionRouter.transcribe(settings, audioFile)
            val cleaned = when (settings.cleanupProvider) {
                CleanupProvider.OPENAI -> openAiCleanupClient.clean(
                    apiKey = settings.openAiApiKey,
                    model = settings.openAiCleanupModel,
                    rawTranscript = raw.text,
                    cleanupPrompt = settings.cleanupPrompt,
                    dictionaryEntries = dictionaryEntries,
                    styleContext = styleContext,
                )
                CleanupProvider.GEMINI -> geminiCleanupClient.clean(
                    apiKey = settings.geminiApiKey,
                    model = settings.geminiCleanupModel,
                    rawTranscript = raw.text,
                    cleanupPrompt = settings.cleanupPrompt,
                    dictionaryEntries = dictionaryEntries,
                    styleContext = styleContext,
                )
            }
            val expandedText = SnippetExpander.expand(cleaned.text, snippetEntries)
            val insertResult = insertionFactory().insert(expandedText)
            historyRepository.save(
                DictationHistoryEntry(
                    rawTranscript = raw.text,
                    cleanedText = expandedText,
                    createdAtMillis = System.currentTimeMillis(),
                    transcriptionProvider = raw.provider,
                    transcriptionModel = raw.model,
                    cleanupProvider = cleaned.provider,
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
