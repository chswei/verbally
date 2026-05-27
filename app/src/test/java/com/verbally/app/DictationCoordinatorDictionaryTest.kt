package com.verbally.app

import com.verbally.app.audio.AudioRecorder
import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.dictionary.InMemoryDictionaryRepository
import com.verbally.app.history.InMemoryDictationHistoryRepository
import com.verbally.app.insertion.ClipboardGateway
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.insertion.PasteTarget
import com.verbally.app.providers.CleanedTranscript
import com.verbally.app.providers.RawTranscript
import com.verbally.app.providers.TextCleanupClient
import com.verbally.app.providers.TranscriptionClient
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.SettingsRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DictationCoordinatorDictionaryTest {
    @Test
    fun confirmRecordingPassesDictionaryEntriesToOpenAiCleanup() = runBlocking {
        val dictionaryRepository = InMemoryDictionaryRepository()
        dictionaryRepository.save(DictionaryEntry(term = "OpenAI", note = "品牌名，不要加空白", id = 1L))
        val openAiCleanup = CapturingCleanupClient(provider = "openai")
        val audioRecorder = FakeAudioRecorder()
        val coordinator = coordinator(
            settings = AppSettings(openAiApiKey = "openai-test", cleanupProvider = CleanupProvider.OPENAI),
            dictionaryRepository = dictionaryRepository,
            audioRecorder = audioRecorder,
            openAiCleanupClient = openAiCleanup,
        )

        coordinator.confirmRecording(appLabel = "Test")

        assertEquals(listOf("OpenAI"), openAiCleanup.dictionaryEntries.map { it.term })
        assertEquals("品牌名，不要加空白", openAiCleanup.dictionaryEntries.single().note)
        assertEquals(true, audioRecorder.deleted)
    }

    @Test
    fun confirmRecordingPassesDictionaryEntriesToGeminiCleanup() = runBlocking {
        val dictionaryRepository = InMemoryDictionaryRepository()
        dictionaryRepository.save(DictionaryEntry(term = "Gemini", note = "Google 模型名稱", id = 1L))
        val geminiCleanup = CapturingCleanupClient(provider = "gemini")
        val coordinator = coordinator(
            settings = AppSettings(
                geminiApiKey = "gemini-test",
                cleanupProvider = CleanupProvider.GEMINI,
            ),
            dictionaryRepository = dictionaryRepository,
            geminiCleanupClient = geminiCleanup,
        )

        coordinator.confirmRecording(appLabel = "Test")

        assertEquals(listOf("Gemini"), geminiCleanup.dictionaryEntries.map { it.term })
        assertEquals("Google 模型名稱", geminiCleanup.dictionaryEntries.single().note)
    }

    private fun coordinator(
        settings: AppSettings,
        dictionaryRepository: InMemoryDictionaryRepository,
        audioRecorder: FakeAudioRecorder = FakeAudioRecorder(),
        openAiCleanupClient: CapturingCleanupClient = CapturingCleanupClient(provider = "openai"),
        geminiCleanupClient: CapturingCleanupClient = CapturingCleanupClient(provider = "gemini"),
    ) = DictationCoordinator(
        settingsRepository = FakeSettingsRepository(settings),
        historyRepository = InMemoryDictationHistoryRepository(),
        dictionaryRepository = dictionaryRepository,
        audioRecorder = audioRecorder,
        transcriptionClient = FakeTranscriptionClient(),
        openAiCleanupClient = openAiCleanupClient,
        geminiCleanupClient = geminiCleanupClient,
        insertionFactory = {
            ClipboardPasteInserter(
                clipboard = object : ClipboardGateway {
                    override var currentText: String? = null
                },
                pasteTarget = object : PasteTarget {
                    override fun pasteFromClipboard(text: String): Boolean = true
                },
            )
        },
    )

    private class FakeSettingsRepository(private val settings: AppSettings) : SettingsRepository {
        override fun load(): AppSettings = settings
        override fun save(settings: AppSettings) = Unit
        override fun clearHistoryRequested() = Unit
    }

    private class FakeAudioRecorder : AudioRecorder {
        private val file = File.createTempFile("verbally-test-", ".m4a")
        var deleted = false

        override fun start(): File = file
        override fun stop(): File = file
        override fun stopAndDelete() = Unit
        override fun currentAmplitude(): Int = 0
        override fun delete(file: File?) {
            deleted = true
            file?.delete()
        }
    }

    private class FakeTranscriptionClient : TranscriptionClient {
        override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
            RawTranscript(text = "請提醒 open ai 的 Sarah", model = model)
    }

    private class CapturingCleanupClient(
        private val provider: String,
    ) : TextCleanupClient {
        var dictionaryEntries: List<DictionaryEntry> = emptyList()

        override suspend fun clean(
            apiKey: String,
            model: String,
            rawTranscript: String,
            cleanupPrompt: String,
            dictionaryEntries: List<DictionaryEntry>,
        ): CleanedTranscript {
            this.dictionaryEntries = dictionaryEntries
            return CleanedTranscript(text = "請提醒 OpenAI 的 Sarah。", provider = provider, model = model)
        }
    }
}
