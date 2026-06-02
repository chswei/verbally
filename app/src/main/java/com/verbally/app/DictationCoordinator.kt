package com.verbally.app

import com.verbally.app.audio.AudioRecorder
import com.verbally.app.audio.RecordedAudioQualityAnalyzer
import com.verbally.app.dictionary.DictionaryRepository
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.history.DictationHistoryRepository
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.providers.TextCleanupClient
import com.verbally.app.providers.TranscriptionClient
import com.verbally.app.providers.TranscriptionClientRouter
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.AppLanguage
import com.verbally.app.settings.SettingsRepository
import com.verbally.app.settings.withDefaultCleanupPromptLanguage
import com.verbally.app.snippets.SnippetExpander
import com.verbally.app.snippets.SnippetRepository
import com.verbally.app.style.AppCategoryClassifier
import com.verbally.app.style.AppStyleRuleRepository
import com.verbally.app.style.AppStyleProfileRepository
import com.verbally.app.style.CleanupStyleContext
import com.verbally.app.style.InMemoryAppStyleRuleRepository
import com.verbally.app.style.normalizedStyleRuleLanguage
import java.io.File

class DictationCoordinator(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: DictationHistoryRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val snippetRepository: SnippetRepository,
    private val styleProfileRepository: AppStyleProfileRepository,
    private val styleRuleRepository: AppStyleRuleRepository = InMemoryAppStyleRuleRepository(),
    private val audioRecorder: AudioRecorder,
    private val transcriptionRouter: TranscriptionClientRouter,
    private val openAiCleanupClient: TextCleanupClient,
    private val geminiCleanupClient: TextCleanupClient,
    private val insertionFactory: () -> ClipboardPasteInserter,
    private val noRecordingMessage: String = "沒有可處理的錄音。",
    private val defaultPromptLanguageFor: (AppLanguage) -> AppLanguage = { language ->
        if (language == AppLanguage.SYSTEM) AppLanguage.ENGLISH else language
    },
) {
    constructor(
        settingsRepository: SettingsRepository,
        historyRepository: DictationHistoryRepository,
        dictionaryRepository: DictionaryRepository,
        snippetRepository: SnippetRepository,
        styleProfileRepository: AppStyleProfileRepository,
        styleRuleRepository: AppStyleRuleRepository = InMemoryAppStyleRuleRepository(),
        audioRecorder: AudioRecorder,
        transcriptionClient: TranscriptionClient,
        openAiCleanupClient: TextCleanupClient,
        geminiCleanupClient: TextCleanupClient,
        insertionFactory: () -> ClipboardPasteInserter,
        noRecordingMessage: String = "沒有可處理的錄音。",
        defaultPromptLanguageFor: (AppLanguage) -> AppLanguage = { language ->
            if (language == AppLanguage.SYSTEM) AppLanguage.ENGLISH else language
        },
    ) : this(
        settingsRepository = settingsRepository,
        historyRepository = historyRepository,
        dictionaryRepository = dictionaryRepository,
        snippetRepository = snippetRepository,
        styleProfileRepository = styleProfileRepository,
        styleRuleRepository = styleRuleRepository,
        audioRecorder = audioRecorder,
        transcriptionRouter = TranscriptionClientRouter.single(transcriptionClient),
        openAiCleanupClient = openAiCleanupClient,
        geminiCleanupClient = geminiCleanupClient,
        insertionFactory = insertionFactory,
        noRecordingMessage = noRecordingMessage,
        defaultPromptLanguageFor = defaultPromptLanguageFor,
    )

    private var currentRecording: File? = null
    private val recordingActivityTracker = RecordingActivityTracker()

    fun startRecording() {
        recordingActivityTracker.reset()
        currentRecording = audioRecorder.start()
    }

    fun cancelRecording() {
        audioRecorder.stopAndDelete()
        currentRecording = null
        recordingActivityTracker.reset()
    }

    fun currentAmplitude(): Int =
        audioRecorder.currentAmplitude().also { recordingActivityTracker.record(it) }

    suspend fun confirmRecording(appLabel: String?): DictationOutcome {
        val recordingWasStarted = currentRecording != null
        if (recordingWasStarted) {
            recordingActivityTracker.record(audioRecorder.currentAmplitude())
        }
        val noAudioSamples = recordingWasStarted && recordingActivityTracker.sampledOnlyZeroAmplitude()
        val audioFile = audioRecorder.stop() ?: currentRecording
        currentRecording = null
        if (audioFile == null) {
            recordingActivityTracker.reset()
            return DictationOutcome.Failure(noRecordingMessage)
        }

        return try {
            val recordingHasNoContent = noAudioSamples ||
                RecordedAudioQualityAnalyzer.analyze(audioFile).clearlyHasNoSpeechContent()
            if (recordingHasNoContent) return DictationOutcome.NoDictatedContent

            val storedSettings = settingsRepository.load()
            val styleRuleLanguage = defaultPromptLanguageFor(storedSettings.interfaceLanguage)
                .normalizedStyleRuleLanguage()
            val settings = storedSettings.withDefaultCleanupPromptLanguage(styleRuleLanguage)
            val dictionaryEntries = dictionaryRepository.list()
            val snippetEntries = snippetRepository.list()
            val appCategory = AppCategoryClassifier.classify(appLabel)
            val outputStyle = styleProfileRepository.styleFor(appCategory)
            val styleContext = CleanupStyleContext(
                category = appCategory,
                style = outputStyle,
                language = styleRuleLanguage,
                customRule = styleRuleRepository.customRuleFor(styleRuleLanguage, outputStyle),
            )
            val raw = transcriptionRouter.transcribe(settings, audioFile)
            if (DictationContentGuard.rawTranscriptHasNoContent(raw)) {
                return DictationOutcome.NoDictatedContent
            }
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
            if (DictationContentGuard.cleanedTextHasNoContent(cleaned.text)) {
                return DictationOutcome.NoDictatedContent
            }
            val insertionText = if (DictationContentGuard.cleanedTextShouldFallBackToRawTranscript(raw.text, cleaned.text)) {
                raw.text
            } else {
                cleaned.text
            }
            val expandedText = SnippetExpander.expand(insertionText, snippetEntries)
            if (DictationContentGuard.cleanedTextHasNoContent(expandedText)) {
                return DictationOutcome.NoDictatedContent
            }
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
            if (insertResult.pasted) {
                DictationOutcome.Inserted(insertResult.message)
            } else {
                DictationOutcome.ClipboardFallback(insertResult.message)
            }
        } finally {
            audioRecorder.delete(audioFile)
            recordingActivityTracker.reset()
        }
    }

    private class RecordingActivityTracker {
        private var sampled = false
        private var nonZeroAmplitudeObserved = false

        fun record(amplitude: Int) {
            sampled = true
            if (amplitude > 0) {
                nonZeroAmplitudeObserved = true
            }
        }

        fun sampledOnlyZeroAmplitude(): Boolean = sampled && !nonZeroAmplitudeObserved

        fun reset() {
            sampled = false
            nonZeroAmplitudeObserved = false
        }
    }
}
