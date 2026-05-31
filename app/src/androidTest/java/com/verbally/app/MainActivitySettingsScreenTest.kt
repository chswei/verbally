package com.verbally.app

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.providers.CleanupPromptFactory
import com.verbally.app.settings.AppLanguage
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.AppThemeMode
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.TranscriptionProvider
import com.verbally.app.snippets.SnippetEntry
import com.verbally.app.style.AppCategory
import com.verbally.app.style.AppStyleProfile
import com.verbally.app.style.AppStyleRule
import com.verbally.app.style.InMemoryAppStyleProfileRepository
import com.verbally.app.style.OutputStyle
import com.verbally.app.style.StyleRuleDefaults
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivitySettingsScreenTest {
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
    fun verballyThemeUsesSelectedLightAndDarkSchemes() {
        var lightPrimary = Color.Unspecified
        var lightBackground = Color.Unspecified
        var lightSecondaryContainer = Color.Unspecified
        var lightTertiaryContainer = Color.Unspecified
        var darkBackground = Color.Unspecified
        var darkSecondaryContainer = Color.Unspecified
        var darkTertiaryContainer = Color.Unspecified

        composeRule.setContent {
            VerballyTheme(themeMode = AppThemeMode.LIGHT) {
                lightPrimary = MaterialTheme.colorScheme.primary
                lightBackground = MaterialTheme.colorScheme.background
                lightSecondaryContainer = MaterialTheme.colorScheme.secondaryContainer
                lightTertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
            }
            VerballyTheme(themeMode = AppThemeMode.DARK) {
                darkBackground = MaterialTheme.colorScheme.background
                darkSecondaryContainer = MaterialTheme.colorScheme.secondaryContainer
                darkTertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
            }
        }

        composeRule.runOnIdle {
            assertEquals(Color(0xFF14233A), lightPrimary)
            assertEquals(Color(0xFFF8FAFC), lightBackground)
            assertEquals(Color(0xFFD6F1EA), lightSecondaryContainer)
            assertEquals(Color(0xFFEDE7FF), lightTertiaryContainer)
            assertEquals(Color(0xFF0F1419), darkBackground)
            assertEquals(Color(0xFF234E48), darkSecondaryContainer)
            assertEquals(Color(0xFF564A88), darkTertiaryContainer)
        }
    }

    @Test
    fun historyScreenClearsHistoryFromOverflowMenuWithConfirmation() {
        var clearClicked = false
        val entries = listOf(
            DictationHistoryEntry(
                rawTranscript = "raw",
                cleanedText = "整理後文字",
                createdAtMillis = 1L,
                transcriptionProvider = "OpenAI",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupProvider = "OpenAI",
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

        composeRule.onAllNodesWithText("清空歷史")
            .assertCountEquals(0)
        val titleCenterY = composeRule.onNodeWithText("歷史")
            .fetchSemanticsNode()
            .boundsInRoot
            .center
            .y
        val menuCenterY = composeRule.onNodeWithContentDescription("歷史選單")
            .fetchSemanticsNode()
            .boundsInRoot
            .center
            .y
        assertTrue(
            "History overflow should align with the title center: title=$titleCenterY menu=$menuCenterY",
            abs(titleCenterY - menuCenterY) <= 4f,
        )
        composeRule.onNodeWithContentDescription("歷史選單")
            .assertIsDisplayed()
            .performClick()
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
        composeRule.onNodeWithContentDescription("歷史選單")
            .performClick()
        composeRule.onNodeWithText("清空歷史")
            .performClick()
        composeRule.onNodeWithText("確定刪除")
            .performClick()

        assertTrue(clearClicked)
    }

    @Test
    fun historyScreenShowsRetentionCopyAndDeleteControls() {
        var deletedEntry: DictationHistoryEntry? = null
        val entries = listOf(
            DictationHistoryEntry(
                rawTranscript = "raw 1",
                cleanedText = "第一筆",
                createdAtMillis = 1L,
                transcriptionProvider = "OpenAI",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupProvider = "OpenAI",
                cleanupModel = "gpt-test",
                appLabel = null,
            ),
            DictationHistoryEntry(
                rawTranscript = "raw 2",
                cleanedText = "第二筆",
                createdAtMillis = 2L,
                transcriptionProvider = "OpenAI",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupProvider = "Gemini",
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
                    onDelete = { deletedEntry = it },
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
        composeRule.onNodeWithContentDescription("刪除 第一筆")
            .performClick()

        composeRule.onNodeWithText("確定刪除這筆歷史？")
            .assertIsDisplayed()
        composeRule.onNodeWithText("取消")
            .performClick()
        composeRule.runOnIdle {
            assertEquals(null, deletedEntry)
        }

        composeRule.onNodeWithContentDescription("刪除 第一筆")
            .performClick()
        composeRule.onNodeWithText("確定刪除")
            .performClick()
        composeRule.runOnIdle {
            assertEquals(entries.first(), deletedEntry)
        }
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
        composeRule.onAllNodesWithContentDescription("歷史選單")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("清空歷史")
            .assertCountEquals(0)
    }

    @Test
    fun historyTitlePositionDoesNotShiftWhenOverflowAppears() {
        val entries = listOf(
            DictationHistoryEntry(
                rawTranscript = "raw",
                cleanedText = "整理後文字",
                createdAtMillis = 1L,
                transcriptionProvider = "OpenAI",
                transcriptionModel = "gpt-4o-transcribe",
                cleanupProvider = "OpenAI",
                cleanupModel = "gpt-test",
                appLabel = null,
            ),
        )
        var visibleEntries by mutableStateOf(emptyList<DictationHistoryEntry>())

        composeRule.setContent {
            MaterialTheme {
                HistoryScreenContent(
                    query = "",
                    entries = visibleEntries,
                    onQueryChange = {},
                    onClearHistory = {},
                    onCopy = {},
                    onDelete = {},
                )
            }
        }

        val titleTopWithoutOverflow = composeRule.onNodeWithText("歷史")
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        composeRule.runOnIdle {
            visibleEntries = entries
        }
        composeRule.waitForIdle()

        val titleTopWithOverflow = composeRule.onNodeWithText("歷史")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val menuCenterY = composeRule.onNodeWithContentDescription("歷史選單")
            .fetchSemanticsNode()
            .boundsInRoot
            .center
            .y
        val titleCenterY = composeRule.onNodeWithText("歷史")
            .fetchSemanticsNode()
            .boundsInRoot
            .center
            .y

        assertTrue(
            "History title should not move when overflow appears: without=$titleTopWithoutOverflow with=$titleTopWithOverflow",
            abs(titleTopWithOverflow - titleTopWithoutOverflow) <= 2f,
        )
        assertTrue(
            "History overflow should align with the title center: title=$titleCenterY menu=$menuCenterY",
            abs(titleCenterY - menuCenterY) <= 4f,
        )
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
        composeRule.onNodeWithText("Soniox: Soniox Realtime")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Groq: whisper-large-v3-turbo")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Deepgram: Real-time Nova-3")
            .assertIsDisplayed()
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

    @Test
    fun mainAppShellUsesHistoryAndSettingsAsPrimaryDestinations() {
        var openedSettings = false
        var showingSettings by mutableStateOf(false)
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = true,
                    showingSettings = showingSettings,
                    selectedDestination = AppDestination.HOME,
                    onDestinationSelected = {},
                    onOpenSettings = {
                        openedSettings = true
                        showingSettings = true
                    },
                    onOpenPermissions = {},
                    homeContent = {},
                    dictionaryContent = {},
                    snippetsContent = {},
                    historyContent = {},
                    styleContent = {},
                    settingsContent = { Text(context.getString(R.string.settings_appearance_mode)) },
                )
            }
        }

        composeRule.onNodeWithText("Verbally")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.nav_home))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.nav_dictionary))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.nav_snippets))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.nav_history))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.nav_style))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText("☰")
            .assertCountEquals(0)
        composeRule.onNodeWithContentDescription("開啟選單")
            .performClick()
        composeRule.onNodeWithText("管理與支援")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("選單")
            .assertCountEquals(0)
        composeRule.onNodeWithText("應用程式")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("設定選單項目")
            .assertIsDisplayed()
        composeRule.onNodeWithText("外觀與 App 偏好")
            .assertIsDisplayed()
        composeRule.onNodeWithText("支援")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("權限與疑難排解選單項目")
            .assertIsDisplayed()
        composeRule.onNodeWithText("權限與疑難排解")
            .assertIsDisplayed()
        composeRule.onNodeWithText("錄音、浮動視窗與輔助使用")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Permission Setup")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("API 與偏好")
            .assertCountEquals(0)
        composeRule.onNodeWithText("設定")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { openedSettings }
        assertTrue(openedSettings)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.settings_appearance_mode))
            .assertIsDisplayed()
    }

    @Test
    fun drawerPermissionSupportOpensPermissionSetup() {
        var openedPermissions = false

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = true,
                    showingSettings = false,
                    selectedDestination = AppDestination.HOME,
                    onDestinationSelected = {},
                    onOpenSettings = {},
                    onOpenPermissions = { openedPermissions = true },
                    homeContent = {},
                    dictionaryContent = {},
                    snippetsContent = {},
                    historyContent = {},
                    styleContent = {},
                    settingsContent = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("開啟選單")
            .performClick()
        composeRule.onNodeWithText("權限與疑難排解")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { openedPermissions }
        assertTrue(openedPermissions)
    }

    @Test
    fun missingPermissionsShowBannerActionInMainShell() {
        var openedPermissions = false

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = false,
                    showingSettings = false,
                    selectedDestination = AppDestination.HOME,
                    onDestinationSelected = {},
                    onOpenSettings = {},
                    onOpenPermissions = { openedPermissions = true },
                    homeContent = {},
                    dictionaryContent = {},
                    snippetsContent = {},
                    historyContent = {},
                    styleContent = {},
                    settingsContent = {},
                )
            }
        }

        composeRule.onNodeWithText("補開權限")
            .performClick()

        assertTrue(openedPermissions)
    }

    @Test
    fun drawerPermissionSupportStaysOpenWhenPermissionsAreAlreadyComplete() {
        assertFalse(
            shouldDismissPermissionScreenOnPermissionRefresh(
                openReason = PermissionScreenOpenReason.SUPPORT,
                permissionsReady = true,
            ),
        )
        assertTrue(
            shouldDismissPermissionScreenOnPermissionRefresh(
                openReason = PermissionScreenOpenReason.REQUIRED_SETUP,
                permissionsReady = true,
            ),
        )
        assertFalse(
            shouldDismissPermissionScreenOnPermissionRefresh(
                openReason = PermissionScreenOpenReason.REQUIRED_SETUP,
                permissionsReady = false,
            ),
        )
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

    @Test
    fun dictionaryShowsSearchEmptyStateAndAddAction() {
        composeRule.setContent {
            MaterialTheme {
                DictionaryScreenContent(
                    query = "",
                    entries = emptyList(),
                    onQueryChange = {},
                    onSave = {},
                    onDelete = {},
                )
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
    fun dictionaryCanAddEditDeleteAndSearchEntries() {
        var entries by mutableStateOf(emptyList<DictionaryEntry>())
        var query by mutableStateOf("")
        fun saveEntry(entry: DictionaryEntry) {
            entries = listOf(entry) + entries.filterNot { it.id == entry.id }
        }

        composeRule.setContent {
            MaterialTheme {
                DictionaryScreenContent(
                    query = query,
                    entries = entries.filter {
                        query.isBlank() ||
                            it.term.contains(query, ignoreCase = true) ||
                            it.note.orEmpty().contains(query, ignoreCase = true)
                    },
                    onQueryChange = { query = it },
                    onSave = ::saveEntry,
                    onDelete = { entry -> entries = entries.filterNot { it.id == entry.id } },
                )
            }
        }

        composeRule.onNodeWithContentDescription("新增字典詞彙")
            .performClick()
        composeRule.onNodeWithText("新增字典詞彙")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("字典詞彙輸入")
            .performTextInput("OpenAI")
        composeRule.onNodeWithContentDescription("字典備註輸入")
            .performTextInput("品牌名，不要加空白")
        composeRule.onNodeWithText("儲存")
            .performClick()

        composeRule.onNodeWithText("OpenAI")
            .assertIsDisplayed()
        composeRule.onNodeWithText("品牌名，不要加空白")
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription("編輯 OpenAI")
            .performClick()
        composeRule.onNodeWithContentDescription("字典詞彙輸入")
            .performTextClearance()
        composeRule.onNodeWithContentDescription("字典詞彙輸入")
            .performTextInput("OpenAI API")
        composeRule.onNodeWithText("儲存")
            .performClick()

        composeRule.onNodeWithText("OpenAI API")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("OpenAI")
            .assertCountEquals(0)

        composeRule.onNodeWithContentDescription("搜尋字典輸入")
            .performTextInput("API")
        composeRule.onNodeWithText("OpenAI API")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("搜尋字典輸入")
            .performTextClearance()
        composeRule.onNodeWithContentDescription("搜尋字典輸入")
            .performTextInput("不存在")
        composeRule.onNodeWithText("找不到符合的字典詞彙")
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription("搜尋字典輸入")
            .performTextClearance()
        composeRule.onNodeWithContentDescription("刪除 OpenAI API")
            .performClick()
        composeRule.onAllNodesWithText("OpenAI API")
            .assertCountEquals(0)
        composeRule.onNodeWithText("尚未建立字典詞彙")
            .assertIsDisplayed()
    }

    @Test
    fun snippetsShowsSearchEmptyStateAndAddAction() {
        composeRule.setContent {
            MaterialTheme {
                SnippetsScreenContent(
                    query = "",
                    entries = emptyList(),
                    onQueryChange = {},
                    onSave = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("搜尋片段")
            .assertIsDisplayed()
        composeRule.onNodeWithText("片段")
            .assertIsDisplayed()
        composeRule.onNodeWithText("保存觸發詞與展開文字；聽寫時說出觸發詞，就會插入完整內容。")
            .assertIsDisplayed()
        composeRule.onNodeWithText("尚未建立常用片段")
            .assertIsDisplayed()
        composeRule.onNodeWithText("新增像「我的地址」或「放射科報告模板」這類觸發詞，之後聽寫會展開成完整文字。")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("新增常用片段")
            .assertIsDisplayed()
    }

    @Test
    fun snippetsCanAddEditDeleteAndSearchEntries() {
        var entries by mutableStateOf(emptyList<SnippetEntry>())
        var query by mutableStateOf("")
        fun saveEntry(entry: SnippetEntry) {
            entries = listOf(entry) + entries.filterNot {
                it.id == entry.id || it.trigger.equals(entry.trigger, ignoreCase = true)
            }
        }

        composeRule.setContent {
            MaterialTheme {
                SnippetsScreenContent(
                    query = query,
                    entries = entries.filter {
                        query.isBlank() ||
                            it.trigger.contains(query, ignoreCase = true) ||
                            it.expansion.contains(query, ignoreCase = true)
                    },
                    onQueryChange = { query = it },
                    onSave = ::saveEntry,
                    onDelete = { entry -> entries = entries.filterNot { it.id == entry.id } },
                )
            }
        }

        composeRule.onNodeWithContentDescription("新增常用片段")
            .performClick()
        composeRule.onNodeWithText("新增常用片段")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("片段觸發詞輸入")
            .performTextInput("我的地址")
        composeRule.onNodeWithContentDescription("片段展開內容輸入")
            .performTextInput("台北市信義區一號")
        composeRule.onNodeWithText("儲存")
            .performClick()

        composeRule.onNodeWithText("我的地址")
            .assertIsDisplayed()
        composeRule.onNodeWithText("台北市信義區一號")
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription("編輯 我的地址")
            .performClick()
        composeRule.onNodeWithContentDescription("片段觸發詞輸入")
            .performTextClearance()
        composeRule.onNodeWithContentDescription("片段觸發詞輸入")
            .performTextInput("住家地址")
        composeRule.onNodeWithText("儲存")
            .performClick()

        composeRule.onNodeWithText("住家地址")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("我的地址")
            .assertCountEquals(0)

        composeRule.onNodeWithContentDescription("搜尋片段輸入")
            .performTextInput("信義")
        composeRule.onNodeWithText("住家地址")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("搜尋片段輸入")
            .performTextClearance()
        composeRule.onNodeWithContentDescription("搜尋片段輸入")
            .performTextInput("不存在")
        composeRule.onNodeWithText("找不到符合的常用片段")
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription("搜尋片段輸入")
            .performTextClearance()
        composeRule.onNodeWithContentDescription("刪除 住家地址")
            .performClick()
        composeRule.onAllNodesWithText("住家地址")
            .assertCountEquals(0)
        composeRule.onNodeWithText("尚未建立常用片段")
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
