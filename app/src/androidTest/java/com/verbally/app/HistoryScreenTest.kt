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
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.verbally.app.history.DictationHistoryEntry
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HistoryScreenTest {
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

}
