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
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.verbally.app.settings.AppLanguage
import com.verbally.app.style.AppCategory
import com.verbally.app.style.AppStyleProfile
import com.verbally.app.style.AppStyleRule
import com.verbally.app.style.InMemoryAppStyleProfileRepository
import com.verbally.app.style.OutputStyle
import com.verbally.app.style.StyleRuleDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StyleScreensTest {
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
    fun styleProfilesShowCategoriesAndSaveSelection() {
        val repository = InMemoryAppStyleProfileRepository()
        var profiles by mutableStateOf(repository.list())

        composeRule.setContent {
            MaterialTheme {
                StyleProfilesScreenContent(
                    profiles = profiles,
                    onProfileChange = { profile ->
                        repository.save(profile)
                        profiles = repository.list()
                    },
                )
            }
        }

        composeRule.onNodeWithText("語氣風格")
            .assertIsDisplayed()
        composeRule.onNodeWithText("聊天")
            .assertIsDisplayed()
        composeRule.onNodeWithText("工作")
            .assertIsDisplayed()
        composeRule.onNodeWithText("其他")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("聊天 口語")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("工作 正式")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("其他 正式")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Formal")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Casual")
            .assertCountEquals(0)

        composeRule.onNodeWithContentDescription("聊天 正式")
            .performClick()

        composeRule.runOnIdle {
            assertEquals(OutputStyle.FORMAL, repository.styleFor(AppCategory.CHAT))
            assertEquals(
                AppStyleProfile(category = AppCategory.CHAT, style = OutputStyle.FORMAL),
                profiles.first { it.category == AppCategory.CHAT },
            )
        }
    }

    @Test
    fun styleProfilesExposeCustomRuleRows() {
        val repository = InMemoryAppStyleProfileRepository()
        val language = AppLanguage.TRADITIONAL_CHINESE
        val rules = OutputStyle.entries.map { style ->
            AppStyleRule(
                language = language,
                style = style,
                rule = StyleRuleDefaults.defaultRuleFor(language, style),
                isCustom = style == OutputStyle.CASUAL,
            )
        }
        var requestedStyle: OutputStyle? = null

        composeRule.setContent {
            MaterialTheme {
                StyleProfilesScreenContent(
                    profiles = repository.list(),
                    styleRules = rules,
                    onProfileChange = {},
                    onEditRule = { requestedStyle = it },
                )
            }
        }

        composeRule.onNodeWithText("自訂語氣規則")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("口語 規則")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("自訂規則")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("開啟 口語 規則")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(OutputStyle.CASUAL, requestedStyle)
        }
    }

    @Test
    fun styleRuleEditorSavesAndRestoresCustomText() {
        val language = AppLanguage.TRADITIONAL_CHINESE
        var savedRule: String? = null
        var restoredDefault = false

        composeRule.setContent {
            MaterialTheme {
                StyleRuleEditorScreen(
                    language = language,
                    rule = AppStyleRule(
                        language = language,
                        style = OutputStyle.CASUAL,
                        rule = StyleRuleDefaults.defaultRuleFor(language, OutputStyle.CASUAL),
                        isCustom = false,
                    ),
                    onBack = {},
                    onSave = { savedRule = it },
                    onRestoreDefault = { restoredDefault = true },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("口語 規則")
            .assertIsDisplayed()
        val ruleTextField = composeRule.onNodeWithContentDescription("口語 規則輸入")
        ruleTextField.performTextClearance()
        ruleTextField.performTextInput("Only punctuation and spacing.")
        composeRule.onNodeWithText("儲存 口語 規則")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals("Only punctuation and spacing.", savedRule)
        }

        composeRule.onNodeWithContentDescription("口語 規則選單")
            .performClick()
        composeRule.onNodeWithText("還原預設")
            .performClick()

        composeRule.runOnIdle {
            assertTrue(restoredDefault)
        }
    }

}
