package com.verbally.app

import com.verbally.app.audio.AudioRecorder
import com.verbally.app.dictionary.InMemoryDictionaryRepository
import com.verbally.app.history.InMemoryDictationHistoryRepository
import com.verbally.app.insertion.ClipboardGateway
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.insertion.DirectTextTarget
import com.verbally.app.providers.CleanedTranscript
import com.verbally.app.providers.RawTranscript
import com.verbally.app.providers.TextCleanupClient
import com.verbally.app.providers.TranscriptionClient
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.SettingsRepository
import com.verbally.app.snippets.InMemorySnippetRepository
import com.verbally.app.snippets.SnippetEntry
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DictationCoordinatorSnippetsTest {
    @Test
    fun confirmRecordingExpandsSnippetsBeforeInsertionAndHistory() = runBlocking {
        val snippets = InMemorySnippetRepository()
        snippets.save(SnippetEntry(trigger = "我的地址", expansion = "台北市信義區一號", id = 1L))
        val history = InMemoryDictationHistoryRepository()
        val insertion = CapturingDirectTextTarget()
        val coordinator = DictationCoordinator(
            settingsRepository = FakeSettingsRepository(
                AppSettings(openAiApiKey = "openai-test", cleanupProvider = CleanupProvider.OPENAI),
            ),
            historyRepository = history,
            dictionaryRepository = InMemoryDictionaryRepository(),
            snippetRepository = snippets,
            audioRecorder = FakeAudioRecorder(),
            transcriptionClient = FakeTranscriptionClient(),
            openAiCleanupClient = FakeCleanupClient(cleanedText = "請寄到我的地址。"),
            geminiCleanupClient = FakeCleanupClient(cleanedText = "unused"),
            insertionFactory = {
                ClipboardPasteInserter(
                    clipboard = object : ClipboardGateway {
                        override var currentText: String? = null
                    },
                    directTextTarget = insertion,
                )
            },
        )

        coordinator.confirmRecording(appLabel = "Test")

        assertEquals("請寄到台北市信義區一號。", insertion.insertedText)
        assertEquals("請寄到台北市信義區一號。", history.list().single().cleanedText)
    }

    private class FakeSettingsRepository(private val settings: AppSettings) : SettingsRepository {
        override fun load(): AppSettings = settings
        override fun save(settings: AppSettings) = Unit
        override fun clearHistoryRequested() = Unit
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

    private class FakeTranscriptionClient : TranscriptionClient {
        override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript =
            RawTranscript(text = "請寄到我的地址", model = model)
    }

    private class FakeCleanupClient(private val cleanedText: String) : TextCleanupClient {
        override suspend fun clean(
            apiKey: String,
            model: String,
            rawTranscript: String,
            cleanupPrompt: String,
            dictionaryEntries: List<com.verbally.app.dictionary.DictionaryEntry>,
        ): CleanedTranscript = CleanedTranscript(text = cleanedText, provider = "openai", model = model)
    }

    private class CapturingDirectTextTarget : DirectTextTarget {
        var insertedText: String? = null

        override suspend fun insertDirectly(text: String): Boolean {
            insertedText = text
            return true
        }
    }
}
