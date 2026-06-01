package com.verbally.app

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.verbally.app.history.HistoryRetentionMode
import com.verbally.app.settings.AppLanguage
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppPreferencesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun useTraditionalChineseResources() {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags("zh-TW")
    }

    @Test
    fun appSettingsContentOpensAppearanceModeChoicesInDialog() {
        var settings by mutableStateOf(AppSettings())
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                AppSettingsScreenContent(
                    settings = settings,
                    onThemeModeSelected = { settings = settings.copy(themeMode = it) },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_appearance_mode))
            .assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.settings_theme_light))
            .assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.settings_theme_dark))
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("儲存外觀設定")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("選擇 外觀模式")
            .assertCountEquals(0)
        composeRule.onNodeWithContentDescription(context.getString(R.string.settings_open_appearance_picker))
            .assertIsDisplayed()
            .performClick()

        composeRule.onAllNodesWithText(context.getString(R.string.settings_appearance_mode))
            .assertCountEquals(2)
        composeRule.onNodeWithContentDescription(context.getString(R.string.settings_choose_appearance, "深色"))
            .assertIsDisplayed()
            .performClick()

        assertEquals(AppThemeMode.DARK, settings.themeMode)
        composeRule.onNodeWithText(context.getString(R.string.settings_theme_dark))
            .assertIsDisplayed()
    }

    @Test
    fun appSettingsChoiceDialogUsesCompactOptionSpacing() {
        var settings by mutableStateOf(AppSettings())
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                AppSettingsScreenContent(
                    settings = settings,
                    onThemeModeSelected = { settings = settings.copy(themeMode = it) },
                )
            }
        }

        composeRule.onNodeWithContentDescription(context.getString(R.string.settings_open_appearance_picker))
            .assertIsDisplayed()
            .performClick()

        val systemTop = composeRule.onNodeWithContentDescription(
            context.getString(
                R.string.settings_choose_appearance,
                context.getString(R.string.settings_theme_system),
            ),
        ).fetchSemanticsNode().positionInRoot.y
        val lightTop = composeRule.onNodeWithContentDescription(
            context.getString(
                R.string.settings_choose_appearance,
                context.getString(R.string.settings_theme_light),
            ),
        ).fetchSemanticsNode().positionInRoot.y
        val maxCompactSpacing = with(composeRule.density) { 52.dp.toPx() }

        assertTrue(
            "Settings dialog radio rows should be compact, but spacing was ${lightTop - systemTop}px",
            lightTop - systemTop <= maxCompactSpacing,
        )
    }

    @Test
    fun appSettingsContentOpensInterfaceLanguageChoicesInDialog() {
        var settings by mutableStateOf(AppSettings())
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                AppSettingsScreenContent(
                    settings = settings,
                    onThemeModeSelected = { settings = settings.copy(themeMode = it) },
                    onInterfaceLanguageSelected = { settings = settings.copy(interfaceLanguage = it) },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_interface_language))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("English")
            .assertCountEquals(0)
        composeRule.onNodeWithContentDescription(context.getString(R.string.settings_open_interface_language_picker))
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        composeRule.onAllNodesWithText(context.getString(R.string.settings_interface_language))
            .assertCountEquals(2)
        composeRule.onNodeWithContentDescription(
            context.getString(
                R.string.settings_choose_interface_language,
                context.getString(R.string.settings_language_system),
            ),
        )
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            context.getString(R.string.settings_choose_interface_language, "English"),
        )
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()

        assertEquals(AppLanguage.ENGLISH, settings.interfaceLanguage)
        composeRule.onNodeWithText("English")
            .assertIsDisplayed()
    }

    @Test
    fun appSettingsContentConfirmsDestructiveHistoryRetentionChanges() {
        var settings by mutableStateOf(AppSettings())
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                AppSettingsScreenContent(
                    settings = settings,
                    onThemeModeSelected = { settings = settings.copy(themeMode = it) },
                    onHistoryRetentionModeSelected = { settings = settings.copy(historyRetentionMode = it) },
                )
            }
        }

        composeRule.onNodeWithContentDescription(context.getString(R.string.settings_open_history_retention_picker))
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithContentDescription(
            context.getString(
                R.string.settings_choose_history_retention,
                context.getString(R.string.settings_history_retention_none),
            ),
        )
            .assertIsDisplayed()
            .performClick()

        composeRule.onNodeWithText(context.getString(R.string.settings_history_retention_confirm_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.cancel))
            .performClick()

        assertEquals(HistoryRetentionMode.LATEST_100, settings.historyRetentionMode)

        composeRule.onNodeWithContentDescription(context.getString(R.string.settings_open_history_retention_picker))
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithContentDescription(
            context.getString(
                R.string.settings_choose_history_retention,
                context.getString(R.string.settings_history_retention_none),
            ),
        )
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_history_retention_confirm_button))
            .performClick()

        assertEquals(HistoryRetentionMode.NONE, settings.historyRetentionMode)
        composeRule.onNodeWithText(context.getString(R.string.settings_history_retention_none))
            .assertIsDisplayed()
    }

}
