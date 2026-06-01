package com.verbally.app

import com.verbally.app.audio.AudioRecorder
import com.verbally.app.dictionary.DictionaryEntry
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
import com.verbally.app.style.CleanupStyleContext
import com.verbally.app.style.InMemoryAppStyleProfileRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DictationCoordinatorEmptyRecordingTest {
    @Test
    fun confirmRecordingSkipsInsertionWhenNoSpeechActivityWasObserved() = runBlocking {
        val history = InMemoryDictationHistoryRepository()
        val audioRecorder = FakeAudioRecorder(amplitudes = listOf(0, 0, 0))
        val transcription = CapturingTranscriptionClient(rawText = "Thank you.")
        val cleanup = CapturingCleanupClient(cleanedText = "Thank you.")
        val insertion = CapturingDirectTextTarget()
        val coordinator = coordinator(
            history = history,
            audioRecorder = audioRecorder,
            transcription = transcription,
            cleanup = cleanup,
            insertion = insertion,
        )

        coordinator.startRecording()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        val result = coordinator.confirmRecording(appLabel = "Test")

        assertTrue(result is DictationOutcome.NoDictatedContent)
        assertEquals(0, transcription.calls)
        assertEquals(0, cleanup.calls)
        assertNull(insertion.insertedText)
        assertTrue(history.list().isEmpty())
        assertTrue(audioRecorder.deleted)
    }

    @Test
    fun confirmRecordingSkipsCleanupNoContentMessageFromBackgroundNoise() = runBlocking {
        val history = InMemoryDictationHistoryRepository()
        val audioRecorder = FakeAudioRecorder(amplitudes = listOf(1_400, 1_800, 1_600))
        val transcription = CapturingTranscriptionClient(rawText = "鳥叫聲和背景雜音")
        val cleanup = CapturingCleanupClient(cleanedText = "目前沒有內容，請輸入內容。")
        val insertion = CapturingDirectTextTarget()
        val coordinator = coordinator(
            history = history,
            audioRecorder = audioRecorder,
            transcription = transcription,
            cleanup = cleanup,
            insertion = insertion,
        )

        coordinator.startRecording()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        val result = coordinator.confirmRecording(appLabel = "Test")

        assertTrue(result is DictationOutcome.NoDictatedContent)
        assertEquals(1, transcription.calls)
        assertEquals(1, cleanup.calls)
        assertNull(insertion.insertedText)
        assertTrue(history.list().isEmpty())
        assertTrue(audioRecorder.deleted)
    }

    @Test
    fun confirmRecordingSkipsKnownNoSpeechHallucinationFromBackgroundNoise() = runBlocking {
        val history = InMemoryDictationHistoryRepository()
        val audioRecorder = FakeAudioRecorder(amplitudes = listOf(1_400, 1_800, 1_600))
        val transcription = CapturingTranscriptionClient(rawText = "Thank you for watching.")
        val cleanup = CapturingCleanupClient(cleanedText = "Thank you for watching.")
        val insertion = CapturingDirectTextTarget()
        val coordinator = coordinator(
            history = history,
            audioRecorder = audioRecorder,
            transcription = transcription,
            cleanup = cleanup,
            insertion = insertion,
        )

        coordinator.startRecording()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        val result = coordinator.confirmRecording(appLabel = "Test")

        assertTrue(result is DictationOutcome.NoDictatedContent)
        assertEquals(1, transcription.calls)
        assertEquals(1, cleanup.calls)
        assertNull(insertion.insertedText)
        assertTrue(history.list().isEmpty())
        assertTrue(audioRecorder.deleted)
    }

    @Test
    fun confirmRecordingSkipsObservedNoSpeechHallucinationsFromBackgroundNoise() = runBlocking {
        val hallucinations = listOf(
            "you" to "You.",
            "the" to "The.",
            "I'm going to go to the next video" to "I'm going to go to the next video.",
        )

        hallucinations.forEach { (rawText, cleanedText) ->
            val history = InMemoryDictationHistoryRepository()
            val audioRecorder = FakeAudioRecorder(amplitudes = listOf(1_400, 1_800, 1_600))
            val transcription = CapturingTranscriptionClient(rawText = rawText)
            val cleanup = CapturingCleanupClient(cleanedText = cleanedText)
            val insertion = CapturingDirectTextTarget()
            val coordinator = coordinator(
                history = history,
                audioRecorder = audioRecorder,
                transcription = transcription,
                cleanup = cleanup,
                insertion = insertion,
            )

            coordinator.startRecording()
            coordinator.currentAmplitude()
            coordinator.currentAmplitude()
            coordinator.currentAmplitude()
            val result = coordinator.confirmRecording(appLabel = "Test")

            assertTrue(result is DictationOutcome.NoDictatedContent)
            assertEquals(1, transcription.calls)
            assertEquals(1, cleanup.calls)
            assertNull(insertion.insertedText)
            assertTrue(history.list().isEmpty())
            assertTrue(audioRecorder.deleted)
        }
    }

    @Test
    fun confirmRecordingContinuesWhenSpeechActivityWasObserved() = runBlocking {
        val history = InMemoryDictationHistoryRepository()
        val audioRecorder = FakeAudioRecorder(amplitudes = listOf(0, 2_500, 2_000))
        val transcription = CapturingTranscriptionClient(rawText = "請寄到我的地址")
        val cleanup = CapturingCleanupClient(cleanedText = "請寄到我的地址。")
        val insertion = CapturingDirectTextTarget()
        val coordinator = coordinator(
            history = history,
            audioRecorder = audioRecorder,
            transcription = transcription,
            cleanup = cleanup,
            insertion = insertion,
        )

        coordinator.startRecording()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        val result = coordinator.confirmRecording(appLabel = "Test")

        assertTrue(result is DictationOutcome.Inserted)
        assertEquals(1, transcription.calls)
        assertEquals(1, cleanup.calls)
        assertEquals("請寄到我的地址。", insertion.insertedText)
        assertEquals("請寄到我的地址。", history.list().single().cleanedText)
    }

    @Test
    fun confirmRecordingContinuesWhenQuietSpeechActivityWasObserved() = runBlocking {
        val history = InMemoryDictationHistoryRepository()
        val audioRecorder = FakeAudioRecorder(amplitudes = listOf(12, 18, 14))
        val transcription = CapturingTranscriptionClient(rawText = "請寄到我的地址")
        val cleanup = CapturingCleanupClient(cleanedText = "請寄到我的地址。")
        val insertion = CapturingDirectTextTarget()
        val coordinator = coordinator(
            history = history,
            audioRecorder = audioRecorder,
            transcription = transcription,
            cleanup = cleanup,
            insertion = insertion,
        )

        coordinator.startRecording()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        val result = coordinator.confirmRecording(appLabel = "Test")

        assertTrue(result is DictationOutcome.Inserted)
        assertEquals(1, transcription.calls)
        assertEquals(1, cleanup.calls)
        assertEquals("請寄到我的地址。", insertion.insertedText)
        assertEquals("請寄到我的地址。", history.list().single().cleanedText)
    }

    @Test
    fun confirmRecordingFallsBackToRawTranscriptWhenCleanupAsksForMissingOriginalContent() = runBlocking {
        val history = InMemoryDictationHistoryRepository()
        val audioRecorder = FakeAudioRecorder(amplitudes = listOf(1_400, 1_800, 1_600))
        val transcription = CapturingTranscriptionClient(rawText = "中文請使用繁體中文")
        val cleanup = CapturingCleanupClient(cleanedText = "請提供您需要處理的原始轉入內容")
        val insertion = CapturingDirectTextTarget()
        val coordinator = coordinator(
            history = history,
            audioRecorder = audioRecorder,
            transcription = transcription,
            cleanup = cleanup,
            insertion = insertion,
        )

        coordinator.startRecording()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        val result = coordinator.confirmRecording(appLabel = "Test")

        assertTrue(result is DictationOutcome.Inserted)
        assertEquals(1, transcription.calls)
        assertEquals(1, cleanup.calls)
        assertEquals("中文請使用繁體中文", insertion.insertedText)
        assertEquals("中文請使用繁體中文", history.list().single().cleanedText)
    }

    @Test
    fun confirmRecordingFallsBackToRawTranscriptWhenCleanupRespondsLikeAssistant() = runBlocking {
        val history = InMemoryDictationHistoryRepository()
        val audioRecorder = FakeAudioRecorder(amplitudes = listOf(1_400, 1_800, 1_600))
        val transcription = CapturingTranscriptionClient(rawText = "中文請使用繁體中文")
        val cleanup = CapturingCleanupClient(cleanedText = "好的，我會使用繁體中文。")
        val insertion = CapturingDirectTextTarget()
        val coordinator = coordinator(
            history = history,
            audioRecorder = audioRecorder,
            transcription = transcription,
            cleanup = cleanup,
            insertion = insertion,
        )

        coordinator.startRecording()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        val result = coordinator.confirmRecording(appLabel = "Test")

        assertTrue(result is DictationOutcome.Inserted)
        assertEquals("中文請使用繁體中文", insertion.insertedText)
        assertEquals("中文請使用繁體中文", history.list().single().cleanedText)
    }

    @Test
    fun confirmRecordingKeepsAssistantLikeTextWhenItMatchesWhatUserSaid() = runBlocking {
        val history = InMemoryDictationHistoryRepository()
        val audioRecorder = FakeAudioRecorder(amplitudes = listOf(1_400, 1_800, 1_600))
        val transcription = CapturingTranscriptionClient(rawText = "好的 我會使用繁體中文")
        val cleanup = CapturingCleanupClient(cleanedText = "好的，我會使用繁體中文。")
        val insertion = CapturingDirectTextTarget()
        val coordinator = coordinator(
            history = history,
            audioRecorder = audioRecorder,
            transcription = transcription,
            cleanup = cleanup,
            insertion = insertion,
        )

        coordinator.startRecording()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        coordinator.currentAmplitude()
        val result = coordinator.confirmRecording(appLabel = "Test")

        assertTrue(result is DictationOutcome.Inserted)
        assertEquals("好的，我會使用繁體中文。", insertion.insertedText)
        assertEquals("好的，我會使用繁體中文。", history.list().single().cleanedText)
    }

    private fun coordinator(
        history: InMemoryDictationHistoryRepository,
        audioRecorder: FakeAudioRecorder,
        transcription: CapturingTranscriptionClient,
        cleanup: CapturingCleanupClient,
        insertion: CapturingDirectTextTarget,
    ) = DictationCoordinator(
        settingsRepository = FakeSettingsRepository(
            AppSettings(openAiApiKey = "openai-test", cleanupProvider = CleanupProvider.OPENAI),
        ),
        historyRepository = history,
        dictionaryRepository = InMemoryDictionaryRepository(),
        snippetRepository = InMemorySnippetRepository(),
        styleProfileRepository = InMemoryAppStyleProfileRepository(),
        audioRecorder = audioRecorder,
        transcriptionClient = transcription,
        openAiCleanupClient = cleanup,
        geminiCleanupClient = CapturingCleanupClient(cleanedText = "unused"),
        insertionFactory = {
            ClipboardPasteInserter(
                clipboard = object : ClipboardGateway {
                    override var currentText: String? = null
                },
                directTextTarget = insertion,
            )
        },
    )

    private class FakeSettingsRepository(private val settings: AppSettings) : SettingsRepository {
        override fun load(): AppSettings = settings
        override fun save(settings: AppSettings) = Unit
    }

    private class FakeAudioRecorder(
        private val amplitudes: List<Int>,
    ) : AudioRecorder {
        private val file = File.createTempFile("verbally-test-", ".m4a")
        private var amplitudeIndex = 0
        var deleted = false

        override fun start(): File = file
        override fun stop(): File = file
        override fun stopAndDelete() = Unit
        override fun currentAmplitude(): Int {
            val amplitude = amplitudes.getOrElse(amplitudeIndex) { amplitudes.lastOrNull() ?: 0 }
            amplitudeIndex += 1
            return amplitude
        }

        override fun delete(file: File?) {
            deleted = true
            file?.delete()
        }
    }

    private class CapturingTranscriptionClient(
        private val rawText: String,
    ) : TranscriptionClient {
        var calls = 0

        override suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript {
            calls += 1
            return RawTranscript(text = rawText, model = model)
        }
    }

    private class CapturingCleanupClient(
        private val cleanedText: String,
    ) : TextCleanupClient {
        var calls = 0

        override suspend fun clean(
            apiKey: String,
            model: String,
            rawTranscript: String,
            cleanupPrompt: String,
            dictionaryEntries: List<DictionaryEntry>,
            styleContext: CleanupStyleContext,
        ): CleanedTranscript {
            calls += 1
            return CleanedTranscript(text = cleanedText, provider = "openai", model = model)
        }
    }

    private class CapturingDirectTextTarget : DirectTextTarget {
        var insertedText: String? = null

        override suspend fun insertDirectly(text: String): Boolean {
            insertedText = text
            return true
        }
    }
}
