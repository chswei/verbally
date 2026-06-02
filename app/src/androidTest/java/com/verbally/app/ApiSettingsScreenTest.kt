package com.verbally.app

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.verbally.app.providers.CleanupPromptFactory
import com.verbally.app.settings.AppLanguage
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.TranscriptionProvider
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ApiSettingsScreenTest {
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
    fun settingsOverviewShowsApiSettings() {
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("API 設定")
            .assertIsDisplayed()
        composeRule.onNodeWithText("先完成語音轉錄，再完成文字處理；兩個都儲存後就能用浮動按鈕聽寫。")
            .assertIsDisplayed()
        composeRule.onNodeWithText("設定順序：語音轉錄 → 文字處理 → 開始聽寫")
            .assertIsDisplayed()
        composeRule.onNodeWithText("選擇語音辨識模型，貼上 OpenAI API Key 後儲存。")
            .assertIsDisplayed()
        composeRule.onNodeWithText("選擇整理文字的模型；切換服務時只會顯示對應的 API Key。")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("API")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("清空歷史")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("資料")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("外觀模式")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("儲存外觀設定")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("權限都完成了")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("補開權限")
            .assertCountEquals(0)
    }

    @Test
    fun settingsOverviewSupportsApiKeyTestActions() {
        var transcriptionTests = 0
        var cleanupTests = 0
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                    transcriptionTestState = ApiKeyTestUiState(message = "OpenAI API Key 可使用", isSuccess = true),
                    cleanupTestState = ApiKeyTestUiState(message = "OpenAI API Key 測試失敗：HTTP 401", isSuccess = false),
                    onTestTranscriptionApiKey = { transcriptionTests++ },
                    onTestCleanupApiKey = { cleanupTests++ },
                )
            }
        }

        composeRule.onNodeWithText("測試語音轉錄 API Key")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("OpenAI API Key 可使用")
            .assertIsDisplayed()
        composeRule.onNodeWithText("測試文字處理 API Key")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("OpenAI API Key 測試失敗：HTTP 401")
            .performScrollTo()
            .assertIsDisplayed()

        composeRule.runOnIdle {
            assertEquals(1, transcriptionTests)
            assertEquals(1, cleanupTests)
        }
    }

    @Test
    fun settingsOverviewShowsApiSettingsAsInlineBlocks() {
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("語音轉錄")
            .assertIsDisplayed()
        composeRule.onNodeWithText("文字處理")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("API Key")
            .assertCountEquals(2)
        composeRule.onAllNodesWithText("OpenAI API Key")
            .assertCountEquals(0)
        composeRule.onNodeWithText("語音轉錄模型")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("選擇 語音轉錄模型")
            .assertIsDisplayed()
            .performClick()
        composeRule.onAllNodesWithText("OpenAI: gpt-4o-mini-transcribe")
            .assertCountEquals(1)
        composeRule.onNodeWithText("OpenAI: gpt-4o-transcribe")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Soniox: stt-async-v4")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Groq: whisper-large-v3-turbo")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Deepgram: Real-time Nova-3")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("whisper-1")
            .assertCountEquals(0)
        composeRule.onNodeWithText("OpenAI: gpt-4o-transcribe")
            .performClick()
        composeRule.onNodeWithText("文字處理模型")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("選擇 文字處理模型")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.onAllNodesWithText("OpenAI: gpt-5.4-nano")
            .assertCountEquals(1)
        composeRule.onNodeWithText("OpenAI: gpt-5.4-mini")
            .assertIsDisplayed()
        composeRule.onNodeWithText("OpenAI: gpt-5.5")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Gemini: gemini-3.1-flash-lite")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Gemini: gemini-3.1-pro-preview")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("gpt-5-mini")
            .assertCountEquals(0)
        composeRule.onNodeWithText("OpenAI: gpt-5.4-mini")
            .performClick()
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini 整理模型")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("整理 Provider")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("OpenAI 整理模型")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("gemini-2.5-flash")
            .assertCountEquals(0)
        composeRule.onNodeWithText("儲存語音轉錄 API Key")
            .assertIsDisplayed()
        composeRule.onNodeWithText("儲存文字處理設定")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("基本文字處理提示詞")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Transcribe")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("第二層處理")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("›")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("OpenAI API Key、轉錄模型")
            .assertCountEquals(0)
    }

    @Test
    fun modelDropdownKeepsTrailingArrowSeparateFromLongModelName() {
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        val fieldBounds = composeRule.onNodeWithContentDescription("選擇 語音轉錄模型")
            .fetchSemanticsNode()
            .boundsInRoot
        val modelBounds = composeRule.onNodeWithText(
            "gpt-4o-mini-transcribe",
            useUnmergedTree = true,
        )
            .fetchSemanticsNode()
            .boundsInRoot
        val arrowBounds = composeRule.onNodeWithContentDescription(
            "語音轉錄模型 展開箭頭",
            useUnmergedTree = true,
        )
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(
            "Model text should leave room before the trailing arrow: model=$modelBounds arrow=$arrowBounds",
            modelBounds.right <= arrowBounds.left - 8f,
        )
        assertTrue(
            "Dropdown arrow should align to the field center: field=$fieldBounds arrow=$arrowBounds",
            abs(fieldBounds.center.y - arrowBounds.center.y) <= 2f,
        )
    }

    @Test
    fun modelDropdownSeparatesProviderAndModelNameWithBreathingRoom() {
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        val modelBounds = composeRule.onNodeWithText(
            "gpt-4o-mini-transcribe",
            useUnmergedTree = true,
        )
            .fetchSemanticsNode()
            .boundsInRoot
        val providerBounds = composeRule.onAllNodesWithText(
            "OpenAI",
            useUnmergedTree = true,
        )
            .fetchSemanticsNodes()
            .map { it.boundsInRoot }
            .first { it.bottom <= modelBounds.top }

        assertTrue(
            "Provider and model name should have enough breathing room: provider=$providerBounds model=$modelBounds",
            providerBounds.bottom <= modelBounds.top - 5f,
        )
    }

    @Test
    fun settingsContentShowsCompactTranscriptionFields() {
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onAllNodesWithText("API Key")
            .assertCountEquals(2)
        composeRule.onNodeWithText("語音轉錄模型")
            .assertIsDisplayed()
        composeRule.onNodeWithText("儲存語音轉錄 API Key")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("OpenAI 轉錄模型")
            .assertCountEquals(0)
    }

    @Test
    fun transcriptionProviderSelectionSwitchesVisibleKeyField() {
        var settings by mutableStateOf(AppSettings(openAiApiKey = "openai-key"))
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("選擇 語音轉錄模型")
            .performClick()
        composeRule.onNodeWithText("Groq: whisper-large-v3-turbo")
            .performClick()
        composeRule.onAllNodes(hasSetTextAction())
            .onFirst()
            .performTextInput("groq-key")

        composeRule.runOnIdle {
            assertEquals(TranscriptionProvider.GROQ, settings.transcriptionProvider)
            assertEquals("whisper-large-v3-turbo", settings.transcriptionModel)
            assertEquals("openai-key", settings.openAiApiKey)
            assertEquals("groq-key", settings.groqApiKey)
        }
    }

    @Test
    fun cleanupSettingsShowsOnlySelectedOpenAiProviderFields() {
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(cleanupProvider = CleanupProvider.OPENAI),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("文字處理模型")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("API Key")
            .assertCountEquals(2)
        composeRule.onNodeWithText("基本文字處理提示詞")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("儲存文字處理設定")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("整理 Provider")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("OpenAI 整理模型")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini 整理模型")
            .assertCountEquals(0)
    }

    @Test
    fun cleanupSettingsShowsOnlySelectedGeminiProviderFields() {
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(cleanupProvider = CleanupProvider.GEMINI),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("文字處理模型")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("基本文字處理提示詞")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("整理 Provider")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini 整理模型")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("OpenAI 整理模型")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("OpenAI API Key")
            .assertCountEquals(0)
    }

    @Test
    fun cleanupProviderSelectionSwitchesVisibleModelFields() {
        var settings by mutableStateOf(AppSettings(cleanupProvider = CleanupProvider.OPENAI))
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("文字處理模型")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)

        composeRule.onNodeWithContentDescription("選擇 文字處理模型")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("Gemini: gemini-3.1-flash-lite")
            .performClick()

        composeRule.onAllNodesWithText("OpenAI 整理模型")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini 整理模型")
            .assertCountEquals(0)
        composeRule.runOnIdle {
            assertEquals(CleanupProvider.GEMINI, settings.cleanupProvider)
            assertEquals("gemini-3.1-flash-lite", settings.geminiCleanupModel)
        }
    }

    @Test
    fun cleanupPromptCanBeEditedAndRestoredToDefault() {
        var settings by mutableStateOf(
            AppSettings(
                cleanupProvider = CleanupProvider.GEMINI,
                geminiApiKey = "gemini-key",
                geminiCleanupModel = "gemini-3.1-flash-lite",
                interfaceLanguage = AppLanguage.TRADITIONAL_CHINESE,
                cleanupPrompt = "自訂提示詞",
                cleanupPromptIsCustom = true,
            ),
        )

        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onSave = {},
                )
            }
        }

        val promptField = composeRule.onNodeWithContentDescription("基本文字處理提示詞輸入")
        promptField
            .performScrollTo()
            .performTextClearance()
        promptField
            .performTextInput("請整理成條列摘要")

        composeRule.runOnIdle {
            assertEquals("請整理成條列摘要", settings.cleanupPrompt)
            assertEquals(CleanupProvider.GEMINI, settings.cleanupProvider)
            assertEquals("gemini-key", settings.geminiApiKey)
            assertEquals("gemini-3.1-flash-lite", settings.geminiCleanupModel)
        }

        composeRule.onNodeWithContentDescription("基本文字處理提示詞選單")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText("還原預設")
            .performScrollTo()
            .performClick()

        composeRule.runOnIdle {
            assertEquals(CleanupPromptFactory.defaultCleanupPrompt, settings.cleanupPrompt)
            assertEquals(CleanupProvider.GEMINI, settings.cleanupProvider)
            assertEquals("gemini-key", settings.geminiApiKey)
            assertEquals("gemini-3.1-flash-lite", settings.geminiCleanupModel)
        }
    }

    @Test
    fun cleanupPromptDefaultActionLivesInOverflowMenuAndUsesInterfaceLanguageDefault() {
        var settings by mutableStateOf(
            AppSettings(
                interfaceLanguage = AppLanguage.ENGLISH,
                cleanupPrompt = CleanupPromptFactory.defaultCleanupPrompt,
                cleanupPromptIsCustom = false,
            ),
        )

        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("基本文字處理提示詞")
            .performScrollTo()
            .assertIsDisplayed()
        val promptText = composeRule.onNodeWithContentDescription("基本文字處理提示詞輸入")
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text
        assertTrue(promptText.contains("Do not translate"))
        assertFalse(promptText.contains("請將以下語音轉錄"))
        composeRule.onAllNodesWithText("還原預設")
            .assertCountEquals(0)
        composeRule.onNodeWithContentDescription("基本文字處理提示詞選單")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("還原預設")
            .assertIsDisplayed()
            .performClick()

        composeRule.runOnIdle {
            assertFalse(settings.cleanupPromptIsCustom)
            assertEquals(
                CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
                settings.cleanupPrompt,
            )
        }
    }

}
