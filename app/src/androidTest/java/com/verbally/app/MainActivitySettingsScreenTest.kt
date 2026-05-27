package com.verbally.app

import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.providers.CleanupPromptFactory
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainActivitySettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun assertLabelAppearsBefore(first: String, second: String) {
        val firstTop = composeRule.onNodeWithText(first).fetchSemanticsNode().positionInRoot.y
        val secondTop = composeRule.onNodeWithText(second).fetchSemanticsNode().positionInRoot.y
        assertTrue("$first should appear before $second", firstTop < secondTop)
    }

    @Test
    fun settingsOverviewIsOnlyForApiSettings() {
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
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("API")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("清空歷史")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("資料")
            .assertCountEquals(0)
    }

    @Test
    fun historyScreenConfirmsBeforeClearingHistory() {
        var clearClicked = false
        val entries = listOf(
            DictationHistoryEntry(
                rawTranscript = "raw",
                cleanedText = "整理後文字",
                createdAtMillis = 1L,
                provider = "OpenAI",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupModel = "gpt-test",
                appLabel = null,
            ),
        )

        composeRule.setContent {
            MaterialTheme {
                HistoryScreenContent(
                    query = "",
                    entries = entries,
                    onQueryChange = {},
                    onClearHistory = { clearClicked = true },
                    onCopy = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("清空歷史")
            .assertIsDisplayed()
            .performClick()

        assertFalse(clearClicked)
        composeRule.onNodeWithText("確定刪除歷史？")
            .assertIsDisplayed()
        composeRule.onNodeWithText("取消")
            .assertIsDisplayed()
            .performClick()

        assertFalse(clearClicked)
        composeRule.onNodeWithText("清空歷史")
            .performClick()
        composeRule.onNodeWithText("確定刪除")
            .performClick()

        assertTrue(clearClicked)
    }

    @Test
    fun historyScreenShowsRetentionCopyAndDeleteControls() {
        val entries = listOf(
            DictationHistoryEntry(
                rawTranscript = "raw 1",
                cleanedText = "第一筆",
                createdAtMillis = 1L,
                provider = "OpenAI",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupModel = "gpt-test",
                appLabel = null,
            ),
            DictationHistoryEntry(
                rawTranscript = "raw 2",
                cleanedText = "第二筆",
                createdAtMillis = 2L,
                provider = "Gemini",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupModel = "gemini-test",
                appLabel = null,
            ),
        )

        composeRule.setContent {
            MaterialTheme {
                HistoryScreenContent(
                    query = "",
                    entries = entries,
                    onQueryChange = {},
                    onClearHistory = {},
                    onCopy = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("只保留最近 100 筆轉錄紀錄")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("OpenAI / gpt-test")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini / gemini-test")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("複製")
            .assertCountEquals(2)
        composeRule.onAllNodesWithText("刪除")
            .assertCountEquals(2)
    }

    @Test
    fun historyScreenShowsEmptyStateWhenNoEntriesExist() {
        composeRule.setContent {
            MaterialTheme {
                HistoryScreenContent(
                    query = "",
                    entries = emptyList(),
                    onQueryChange = {},
                    onClearHistory = {},
                    onCopy = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("搜尋歷史")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("搜尋、複製或刪除之前的轉錄結果。")
            .assertCountEquals(0)
        composeRule.onNodeWithText("尚無轉錄歷史")
            .assertIsDisplayed()
        composeRule.onNodeWithText("完成聽寫後，整理好的文字會保存在這裡，最多保留最近 100 筆。")
            .assertIsDisplayed()
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
        composeRule.onNodeWithText("gpt-4o-mini-transcribe")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithContentDescription("選擇 語音轉錄模型")
            .performClick()
        composeRule.onAllNodesWithText("whisper-1")
            .assertCountEquals(0)
        composeRule.onNodeWithText("gpt-4o-mini-transcribe")
            .performClick()
        composeRule.onNodeWithText("文字處理模型")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("選擇 文字處理模型")
            .assertIsDisplayed()
            .performClick()
        composeRule.onAllNodesWithText("OpenAI: gpt-5.4-nano")
            .assertCountEquals(2)
        composeRule.onNodeWithText("OpenAI: gpt-5.4-mini")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Gemini: gemini-3.1-flash-lite")
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
        composeRule.onNodeWithText("文字處理提示詞")
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
    fun transcriptionSettingsOnlyShowsTranscriptionFields() {
        composeRule.setContent {
            MaterialTheme {
                TranscriptionSettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("API Key")
            .assertIsDisplayed()
        composeRule.onNodeWithText("語音轉錄模型")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("OpenAI 轉錄模型")
            .assertCountEquals(0)
    }

    @Test
    fun cleanupSettingsShowsOnlySelectedOpenAiProviderFields() {
        composeRule.setContent {
            MaterialTheme {
                CleanupSettingsScreenContent(
                    settings = AppSettings(cleanupProvider = CleanupProvider.OPENAI),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("文字處理模型")
            .assertIsDisplayed()
        composeRule.onNodeWithText("API Key")
            .assertIsDisplayed()
        composeRule.onNodeWithText("文字處理提示詞")
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
                CleanupSettingsScreenContent(
                    settings = AppSettings(cleanupProvider = CleanupProvider.GEMINI),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("文字處理模型")
            .assertIsDisplayed()
        composeRule.onNodeWithText("API Key")
            .assertIsDisplayed()
        assertLabelAppearsBefore("文字處理模型", "API Key")
        composeRule.onNodeWithText("文字處理提示詞")
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
        composeRule.setContent {
            var settings by mutableStateOf(AppSettings(cleanupProvider = CleanupProvider.OPENAI))
            MaterialTheme {
                CleanupSettingsScreenContent(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("文字處理模型")
            .assertIsDisplayed()
        composeRule.onNodeWithText("API Key")
            .assertIsDisplayed()
        assertLabelAppearsBefore("文字處理模型", "API Key")
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)

        composeRule.onNodeWithContentDescription("選擇 文字處理模型")
            .performClick()
        composeRule.onNodeWithText("Gemini: gemini-3.1-flash-lite")
            .performClick()

        composeRule.onNodeWithText("API Key")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("OpenAI 整理模型")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Gemini 整理模型")
            .assertCountEquals(0)
    }

    @Test
    fun cleanupPromptCanBeEditedAndRestoredToDefault() {
        var settings by mutableStateOf(
            AppSettings(
                cleanupProvider = CleanupProvider.GEMINI,
                geminiApiKey = "gemini-key",
                geminiCleanupModel = "gemini-3.1-flash-lite",
                cleanupPrompt = "自訂提示詞",
            ),
        )

        composeRule.setContent {
            MaterialTheme {
                CleanupSettingsScreenContent(
                    settings = settings,
                    onSettingsChange = { settings = it },
                    onSave = {},
                )
            }
        }

        val promptField = composeRule.onNodeWithContentDescription("文字處理提示詞輸入")
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
    fun mainAppShellUsesHistoryAndSettingsAsPrimaryDestinations() {
        var openedSettings = false

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = true,
                    selectedDestination = AppDestination.HOME,
                    onDestinationSelected = {},
                    onOpenSettings = { openedSettings = true },
                    onOpenPermissions = {},
                    homeContent = {},
                    dictionaryContent = {},
                    snippetsContent = {},
                    historyContent = {},
                )
            }
        }

        composeRule.onNodeWithText("Verbally")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("首頁")
            .assertCountEquals(1)
        composeRule.onAllNodesWithText("字典")
            .assertCountEquals(1)
        composeRule.onAllNodesWithText("片段")
            .assertCountEquals(1)
        composeRule.onAllNodesWithText("歷史")
            .assertCountEquals(1)
        composeRule.onAllNodesWithText("Home")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Dictionary")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Snippets")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("History")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("☰")
            .assertCountEquals(0)
        composeRule.onNodeWithContentDescription("開啟選單")
            .performClick()
        composeRule.onNodeWithText("選單")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Permission Setup")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("Settings")
            .assertCountEquals(0)
        composeRule.onNodeWithText("設定")
            .assertIsDisplayed()
            .performClick()

        assertTrue(openedSettings)
    }

    @Test
    fun missingPermissionsShowBannerActionInMainShell() {
        var openedPermissions = false

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = false,
                    selectedDestination = AppDestination.HOME,
                    onDestinationSelected = {},
                    onOpenSettings = {},
                    onOpenPermissions = { openedPermissions = true },
                    homeContent = {},
                    dictionaryContent = {},
                    snippetsContent = {},
                    historyContent = {},
                )
            }
        }

        composeRule.onNodeWithText("補開權限")
            .performClick()

        assertTrue(openedPermissions)
    }

    @Test
    fun dictionaryShowsSearchEmptyStateAndAddAction() {
        composeRule.setContent {
            MaterialTheme {
                DictionaryScreenContent(query = "", onQueryChange = {}, onAdd = {})
            }
        }

        composeRule.onNodeWithText("搜尋字典")
            .assertIsDisplayed()
        composeRule.onNodeWithText("字典")
            .assertIsDisplayed()
        composeRule.onNodeWithText("保存常用詞、專有名詞與偏好的寫法，讓之後整理文字時更好找。")
            .assertIsDisplayed()
        composeRule.onNodeWithText("尚未建立字典詞彙")
            .assertIsDisplayed()
        composeRule.onNodeWithText("新增常用詞或專有名詞後，之後可以在這裡快速查找。")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("新增字典詞彙")
            .assertIsDisplayed()
    }

    @Test
    fun snippetsShowsSearchEmptyStateAndAddAction() {
        composeRule.setContent {
            MaterialTheme {
                SnippetsScreenContent(query = "", onQueryChange = {}, onAdd = {})
            }
        }

        composeRule.onNodeWithText("搜尋片段")
            .assertIsDisplayed()
        composeRule.onNodeWithText("片段")
            .assertIsDisplayed()
        composeRule.onNodeWithText("保存常用句、模板或固定回覆，之後可以快速查找。")
            .assertIsDisplayed()
        composeRule.onNodeWithText("尚未建立常用片段")
            .assertIsDisplayed()
        composeRule.onNodeWithText("新增常用句或模板後，之後可以在這裡快速查找。")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("新增常用片段")
            .assertIsDisplayed()
    }

    @Test
    fun permissionSetupOnlyShowsMicrophoneWhenItIsNext() {
        composeRule.setContent {
            MaterialTheme {
                PermissionSetupContent(
                    microphoneGranted = false,
                    overlayGranted = false,
                    accessibilityGranted = false,
                    primaryActionLabel = "開啟權限",
                    onContinue = {},
                    onOpenAppDetails = {},
                )
            }
        }

        composeRule.onNodeWithText("允許錄音")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("浮動視窗")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("輔助使用")
            .assertCountEquals(0)
    }

    @Test
    fun permissionSetupOnlyShowsOverlayWhenItIsNext() {
        composeRule.setContent {
            MaterialTheme {
                PermissionSetupContent(
                    microphoneGranted = true,
                    overlayGranted = false,
                    accessibilityGranted = false,
                    primaryActionLabel = "開啟設定",
                    onContinue = {},
                    onOpenAppDetails = {},
                )
            }
        }

        composeRule.onNodeWithText("允許浮動視窗")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("麥克風")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("輔助使用")
            .assertCountEquals(0)
    }

    @Test
    fun permissionResumeRefreshEffectRefreshesWhenAppReturnsToForeground() {
        val lifecycleOwner = TestLifecycleOwner()
        var refreshCount = 0

        composeRule.setContent {
            PermissionResumeRefreshEffect(lifecycleOwner = lifecycleOwner) {
                refreshCount += 1
            }
        }

        composeRule.runOnIdle {
            assertEquals(0, refreshCount)
            lifecycleOwner.registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        composeRule.runOnIdle {
            assertEquals(1, refreshCount)
        }
    }

    private class TestLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = registry
    }
}
