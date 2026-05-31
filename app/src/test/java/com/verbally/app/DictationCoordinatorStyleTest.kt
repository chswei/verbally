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
import com.verbally.app.settings.AppLanguage
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.SettingsRepository
import com.verbally.app.snippets.InMemorySnippetRepository
import com.verbally.app.style.AppCategory
import com.verbally.app.style.CleanupStyleContext
import com.verbally.app.style.InMemoryAppStyleRuleRepository
import com.verbally.app.style.InMemoryAppStyleProfileRepository
import com.verbally.app.style.OutputStyle
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DictationCoordinatorStyleTest {
    @Test
    fun confirmRecordingPassesChatCasualStyleToOpenAiCleanup() = runBlocking {
        val cleanup = CapturingCleanupClient(provider = "openai")
        val coordinator = coordinator(
            settings = AppSettings(openAiApiKey = "openai-test", cleanupProvider = CleanupProvider.OPENAI),
            openAiCleanupClient = cleanup,
        )

        coordinator.confirmRecording(appLabel = "jp.naver.line.android")

        assertEquals(AppCategory.CHAT, cleanup.styleContext.category)
        assertEquals(OutputStyle.CASUAL, cleanup.styleContext.style)
    }

    @Test
    fun confirmRecordingPassesWorkFormalStyleToGeminiCleanup() = runBlocking {
        val cleanup = CapturingCleanupClient(provider = "gemini")
        val coordinator = coordinator(
            settings = AppSettings(
                geminiApiKey = "gemini-test",
                cleanupProvider = CleanupProvider.GEMINI,
            ),
            geminiCleanupClient = cleanup,
        )

        coordinator.confirmRecording(appLabel = "com.google.android.gm")

        assertEquals(AppCategory.WORK, cleanup.styleContext.category)
        assertEquals(OutputStyle.FORMAL, cleanup.styleContext.style)
    }

    @Test
    fun confirmRecordingPassesCustomRuleForCurrentInterfaceLanguageAndStyle() = runBlocking {
        val cleanup = CapturingCleanupClient(provider = "openai")
        val styleRuleRepository = InMemoryAppStyleRuleRepository().apply {
            saveCustomRule(
                language = AppLanguage.TRADITIONAL_CHINESE,
                style = OutputStyle.CASUAL,
                rule = "只改標點和空格，不要改字。",
            )
        }
        val coordinator = coordinator(
            settings = AppSettings(
                openAiApiKey = "openai-test",
                cleanupProvider = CleanupProvider.OPENAI,
                interfaceLanguage = AppLanguage.TRADITIONAL_CHINESE,
            ),
            styleRuleRepository = styleRuleRepository,
            openAiCleanupClient = cleanup,
        )

        coordinator.confirmRecording(appLabel = "jp.naver.line.android")

        assertEquals(AppLanguage.TRADITIONAL_CHINESE, cleanup.styleContext.language)
        assertEquals("只改標點和空格，不要改字。", cleanup.styleContext.customRule)
    }

    @Test
    fun confirmRecordingResolvesFollowSystemLanguageForDefaultCleanupPromptAndStyleRules() = runBlocking {
        val cleanup = CapturingCleanupClient(provider = "openai")
        val styleRuleRepository = InMemoryAppStyleRuleRepository().apply {
            saveCustomRule(
                language = AppLanguage.ENGLISH,
                style = OutputStyle.CASUAL,
                rule = "English system casual rule",
            )
        }
        val coordinator = coordinator(
            settings = AppSettings(
                openAiApiKey = "openai-test",
                cleanupProvider = CleanupProvider.OPENAI,
                interfaceLanguage = AppLanguage.SYSTEM,
            ),
            styleRuleRepository = styleRuleRepository,
            defaultPromptLanguageFor = { AppLanguage.ENGLISH },
            openAiCleanupClient = cleanup,
        )

        coordinator.confirmRecording(appLabel = "jp.naver.line.android")

        assertEquals(AppLanguage.ENGLISH, cleanup.styleContext.language)
        assertEquals("English system casual rule", cleanup.styleContext.customRule)
        assertTrue(cleanup.cleanupPrompt.contains("Basic text-processing rules:"))
        assertFalse(cleanup.cleanupPrompt.contains("基本文字處理規則："))
    }

    private fun coordinator(
        settings: AppSettings,
        styleRuleRepository: InMemoryAppStyleRuleRepository = InMemoryAppStyleRuleRepository(),
        defaultPromptLanguageFor: (AppLanguage) -> AppLanguage = { language -> language },
        openAiCleanupClient: CapturingCleanupClient = CapturingCleanupClient(provider = "openai"),
        geminiCleanupClient: CapturingCleanupClient = CapturingCleanupClient(provider = "gemini"),
    ) = DictationCoordinator(
        settingsRepository = FakeSettingsRepository(settings),
        historyRepository = InMemoryDictationHistoryRepository(),
        dictionaryRepository = InMemoryDictionaryRepository(),
        snippetRepository = InMemorySnippetRepository(),
        styleProfileRepository = InMemoryAppStyleProfileRepository(),
        styleRuleRepository = styleRuleRepository,
        defaultPromptLanguageFor = defaultPromptLanguageFor,
        audioRecorder = FakeAudioRecorder(),
        transcriptionClient = FakeTranscriptionClient(),
        openAiCleanupClient = openAiCleanupClient,
        geminiCleanupClient = geminiCleanupClient,
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
            RawTranscript(text = "請幫我傳給 Sarah", model = model)
    }

    private class CapturingCleanupClient(
        private val provider: String,
    ) : TextCleanupClient {
        var styleContext: CleanupStyleContext = CleanupStyleContext.default()
        var cleanupPrompt: String = ""

        override suspend fun clean(
            apiKey: String,
            model: String,
            rawTranscript: String,
            cleanupPrompt: String,
            dictionaryEntries: List<DictionaryEntry>,
            styleContext: CleanupStyleContext,
        ): CleanedTranscript {
            this.styleContext = styleContext
            this.cleanupPrompt = cleanupPrompt
            return CleanedTranscript(text = "請幫我傳給 Sarah。", provider = provider, model = model)
        }
    }
}
