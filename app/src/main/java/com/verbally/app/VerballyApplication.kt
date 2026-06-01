package com.verbally.app

import android.app.Application
import com.verbally.app.dictionary.SharedPreferencesDictionaryRepository
import com.verbally.app.history.SharedPreferencesDictationHistoryRepository
import com.verbally.app.providers.AndroidProviderMessages
import com.verbally.app.providers.GeminiTextCleanupClient
import com.verbally.app.providers.GroqTranscriptionClient
import com.verbally.app.providers.OpenAiTextCleanupClient
import com.verbally.app.providers.OpenAiTranscriptionClient
import com.verbally.app.providers.ProviderApiKeyTester
import com.verbally.app.providers.SonioxRealtimeTranscriptionClient
import com.verbally.app.providers.TranscriptionClientRouter
import com.verbally.app.settings.EncryptedSettingsRepository
import com.verbally.app.snippets.SharedPreferencesSnippetRepository
import com.verbally.app.style.SharedPreferencesAppStyleProfileRepository
import com.verbally.app.style.SharedPreferencesAppStyleRuleRepository

class VerballyApplication : Application() {
    lateinit var container: VerballyContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = VerballyContainer(this)
    }
}

class VerballyContainer(application: Application) {
    private val providerMessages = AndroidProviderMessages(application)
    val settingsRepository = EncryptedSettingsRepository(application)
    val historyRepository = SharedPreferencesDictationHistoryRepository(
        context = application,
        retentionModeProvider = { settingsRepository.load().historyRetentionMode },
    )
    val dictionaryRepository = SharedPreferencesDictionaryRepository(application)
    val snippetRepository = SharedPreferencesSnippetRepository(application)
    val styleProfileRepository = SharedPreferencesAppStyleProfileRepository(application)
    val styleRuleRepository = SharedPreferencesAppStyleRuleRepository(application)
    val transcriptionRouter = TranscriptionClientRouter(
        openAiClient = OpenAiTranscriptionClient(messages = providerMessages),
        sonioxClient = SonioxRealtimeTranscriptionClient(messages = providerMessages),
        groqClient = GroqTranscriptionClient(messages = providerMessages),
    )
    val openAiCleanupClient = OpenAiTextCleanupClient(messages = providerMessages)
    val geminiCleanupClient = GeminiTextCleanupClient(messages = providerMessages)
    val providerKeyTester = ProviderApiKeyTester()
}
