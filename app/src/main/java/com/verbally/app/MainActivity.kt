package com.verbally.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.verbally.app.settings.normalizedModelChoices
import com.verbally.app.settings.withDefaultCleanupPromptLanguage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as VerballyApplication).container
        val savedSettings = container.settingsRepository.load().normalizedModelChoices()
        applyAppLanguage(savedSettings.interfaceLanguage)
        val loadedSettings = savedSettings.withDefaultCleanupPromptLanguage(
            defaultPromptLanguageFor(savedSettings.interfaceLanguage),
        )
        setContent {
            var appSettings by remember { mutableStateOf(loadedSettings) }
            VerballyTheme(themeMode = appSettings.themeMode) {
                VerballyApp(
                    container = container,
                    appSettings = appSettings,
                    onSettingsSaved = { appSettings = it.normalizedModelChoices() },
                )
            }
        }
    }
}
