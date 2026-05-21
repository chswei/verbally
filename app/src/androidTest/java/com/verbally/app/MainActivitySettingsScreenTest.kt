package com.verbally.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.verbally.app.settings.AppSettings
import org.junit.Rule
import org.junit.Test

class MainActivitySettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longApiKeysKeepSaveActionReachableByScrolling() {
        val longApiKey = "sk-" + "a".repeat(240)

        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(
                        openAiApiKey = longApiKey,
                        geminiApiKey = longApiKey,
                    ),
                    onSettingsChange = {},
                    onSave = {},
                    onClearHistory = {},
                )
            }
        }

        composeRule.onNodeWithText("儲存設定")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
