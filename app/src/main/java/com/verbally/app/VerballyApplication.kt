package com.verbally.app

import android.app.Application
import com.verbally.app.dictionary.SharedPreferencesDictionaryRepository
import com.verbally.app.history.SharedPreferencesDictationHistoryRepository
import com.verbally.app.providers.GeminiTextCleanupClient
import com.verbally.app.providers.OpenAiTextCleanupClient
import com.verbally.app.providers.OpenAiTranscriptionClient
import com.verbally.app.settings.EncryptedSettingsRepository
import com.verbally.app.snippets.SharedPreferencesSnippetRepository
import com.verbally.app.style.SharedPreferencesAppStyleProfileRepository

class VerballyApplication : Application() {
    lateinit var container: VerballyContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = VerballyContainer(this)
    }
}

class VerballyContainer(application: Application) {
    val settingsRepository = EncryptedSettingsRepository(application)
    val historyRepository = SharedPreferencesDictationHistoryRepository(application)
    val dictionaryRepository = SharedPreferencesDictionaryRepository(application)
    val snippetRepository = SharedPreferencesSnippetRepository(application)
    val styleProfileRepository = SharedPreferencesAppStyleProfileRepository(application)
    val transcriptionClient = OpenAiTranscriptionClient()
    val openAiCleanupClient = OpenAiTextCleanupClient()
    val geminiCleanupClient = GeminiTextCleanupClient()
}
