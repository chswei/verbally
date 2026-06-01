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
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.snippets.SnippetEntry
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DataScreensTest {
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
    fun dictionaryShowsSearchEmptyStateAndAddAction() {
        composeRule.setContent {
            MaterialTheme {
                DictionaryScreenContent(
                    query = "",
                    entries = emptyList(),
                    onQueryChange = {},
                    onSave = { LocalEntrySaveResult.Saved },
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
                    onSave = {
                        saveEntry(it)
                        LocalEntrySaveResult.Saved
                    },
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
    fun dictionaryShowsValidationMessageWhenSaveConflictsWithSnippet() {
        composeRule.setContent {
            MaterialTheme {
                DictionaryScreenContent(
                    query = "",
                    entries = emptyList(),
                    onQueryChange = {},
                    onSave = { LocalEntrySaveResult.Conflict },
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("新增字典詞彙")
            .performClick()
        composeRule.onNodeWithContentDescription("字典詞彙輸入")
            .performTextInput("我的地址")
        composeRule.onNodeWithText("儲存")
            .performClick()

        composeRule.onNodeWithText("這個名稱已經被字典或片段使用，請換一個。")
            .assertIsDisplayed()
        composeRule.onNodeWithText("新增字典詞彙")
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
                    onSave = { LocalEntrySaveResult.Saved },
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
                    onSave = {
                        saveEntry(it)
                        LocalEntrySaveResult.Saved
                    },
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
    fun snippetsShowsValidationMessageWhenSaveIsDuplicate() {
        composeRule.setContent {
            MaterialTheme {
                SnippetsScreenContent(
                    query = "",
                    entries = emptyList(),
                    onQueryChange = {},
                    onSave = { LocalEntrySaveResult.Duplicate },
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("新增常用片段")
            .performClick()
        composeRule.onNodeWithContentDescription("片段觸發詞輸入")
            .performTextInput("我的地址")
        composeRule.onNodeWithContentDescription("片段展開內容輸入")
            .performTextInput("台北市信義區一號")
        composeRule.onNodeWithText("儲存")
            .performClick()

        composeRule.onNodeWithText("這個名稱已經存在，請換一個。")
            .assertIsDisplayed()
        composeRule.onNodeWithText("新增常用片段")
            .assertIsDisplayed()
    }

}
