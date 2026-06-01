package com.verbally.app

import com.verbally.app.audio.AudioRecorder
import com.verbally.app.dictionary.InMemoryDictionaryRepository
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.history.DictationHistoryRepository
import com.verbally.app.insertion.ClipboardGateway
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.insertion.DirectTextTarget
import com.verbally.app.providers.CleanedTranscript
import com.verbally.app.providers.RawTranscript
import com.verbally.app.providers.TextCleanupClient
import com.verbally.app.providers.TranscriptionClient
import com.verbally.app.providers.TranscriptionClientRouter
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.SettingsRepository
import com.verbally.app.settings.TranscriptionProvider
import com.verbally.app.snippets.InMemorySnippetRepository
import com.verbally.app.style.CleanupStyleContext
import com.verbally.app.style.InMemoryAppStyleProfileRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DictationCoordinatorTranscriptionProviderTest {
    @Test
    fun confirmRecordingRoutesToSelectedTranscriptionProviderKeyAndModel() = runBlocking {
        val groq = CapturingTranscriptionClient(provider = "groq")
        val history = CapturingHistoryRepository()
        val coordinator = coordinator(
            settings = AppSettings(
                openAiApiKey = "openai-key",
                groqApiKey = "groq-key",
                transcriptionProvider = TranscriptionProvider.GROQ,
                transcriptionModel = "whisper-large-v3-turbo",
                cleanupProvider = CleanupProvider.OPENAI,
            ),
            router = TranscriptionClientRouter(
                openAiClient = CapturingTranscriptionClient(provider = "openai"),
                sonioxClient = CapturingTranscriptionClient(provider = "soniox"),
                groqClient = groq,
            ),
            historyRepository = history,
        )

        coordinator.confirmRecording(appLabel = null)

        assertEquals("groq-key", groq.apiKey)
        assertEquals("whisper-large-v3-turbo", groq.model)
        assertEquals("groq", history.saved.transcriptionProvider)
        assertEquals("whisper-large-v3-turbo", history.saved.transcriptionModel)
        assertEquals("openai", history.saved.cleanupProvider)
        assertEquals("gpt-5.4-nano", history.saved.cleanupModel)
    }

    private fun coordinator(
        settings: AppSettings,
        router: TranscriptionClientRouter,
        historyRepository: DictationHistoryRepository = CapturingHistoryRepository(),
    ) = DictationCoordinator(
        settingsRepository = FakeSettingsRepository(settings),
        historyRepository = historyRepository,
        dictionaryRepository = InMemoryDictionaryRepository(),
        snippetRepository = InMemorySnippetRepository(),
        styleProfileRepository = InMemoryAppStyleProfileRepository(),
        audioRecorder = FakeAudioRecorder(),
        transcriptionRouter = router,
        openAiCleanupClient = FakeCleanupClient(provider = "openai"),
        geminiCleanupClient = FakeCleanupClient(provider = "gemini"),
        insertionFactory = {
            ClipboardPasteInserter(
                clipboard = object : ClipboardGateway {
                    override var currentText: String? = null
                },
                directTextTarget = object : DirectTextTarget {
                    override suspend fun insertDirectly(text: String): Boolean = true
                },
            )
        },
    )

    private class FakeSettingsRepository(private val settings: AppSettings) : SettingsRepository {
        override fun load(): AppSettings = settings
        override fun save(settings: AppSettings) = Unit
    }

    private class CapturingHistoryRepository : DictationHistoryRepository {
        lateinit var saved: DictationHistoryEntry

        override fun save(entry: DictationHistoryEntry) {
            saved = entry
        }

        override fun list(): List<DictationHistoryEntry> = if (::saved.isInitialized) listOf(saved) else emptyList()
        override fun search(query: String): List<DictationHistoryEntry> = emptyList()
        override fun delete(id: Long) = Unit
        override fun clear() = Unit
    }

    private class FakeAudioRecorder : AudioRecorder {
        private val file = File.createTempFile("verbally-test-", ".m4a")
        override fun start(): File = file
        override fun stop(): File = file
        override fun stopAndDelete() = Unit
        override fun currentAmplitude(): Int = 0
        override fun delete(file: File?) {
            file?.delete()
        }
    }

    private class CapturingTranscriptionClient(private val provider: String) : TranscriptionClient {
        var apiKey: String = ""
        var model: String = ""

        override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript {
            this.apiKey = apiKey
            this.model = model
            return RawTranscript(text = "請幫我傳給 Sarah", provider = provider, model = model)
        }
    }

    private class FakeCleanupClient(private val provider: String) : TextCleanupClient {
        override suspend fun clean(
            apiKey: String,
            model: String,
            rawTranscript: String,
            cleanupPrompt: String,
            dictionaryEntries: List<com.verbally.app.dictionary.DictionaryEntry>,
            styleContext: CleanupStyleContext,
        ): CleanedTranscript = CleanedTranscript(text = "請幫我傳給 Sarah。", provider = provider, model = model)
    }
}
