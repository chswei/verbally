package com.verbally.app.providers

import com.verbally.app.dictionary.DictionaryEntry
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanupPromptFactoryTest {
    @Test
    fun naturalCleanupPromptPreservesMixedLanguageAndForbidsTranslation() {
        val prompt = CleanupPromptFactory.naturalCleanupPrompt(
            "我等一下要 send 給 Alex 然後 uh 補一下 deadline",
        )

        assertTrue(prompt.contains("保留原本語言"))
        assertTrue(prompt.contains("中英混用"))
        assertTrue(prompt.contains("不要翻譯"))
        assertTrue(prompt.contains("去除口頭禪"))
        assertTrue(prompt.contains("我等一下要 send 給 Alex"))
    }

    @Test
    fun customCleanupPromptReplacesTranscriptPlaceholder() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "請整理成三點：${CleanupPromptFactory.TranscriptPlaceholder}",
            rawTranscript = "第一點 第二點",
        )

        assertTrue(prompt.contains("請整理成三點：第一點 第二點"))
        assertTrue(!prompt.contains(CleanupPromptFactory.TranscriptPlaceholder))
    }

    @Test
    fun customCleanupPromptWithoutPlaceholderAppendsTranscript() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "請整理成正式 email",
            rawTranscript = "明天 meeting 改十點",
        )

        assertTrue(prompt.contains("請整理成正式 email"))
        assertTrue(prompt.contains("原始轉錄："))
        assertTrue(prompt.contains("明天 meeting 改十點"))
    }

    @Test
    fun cleanupPromptIncludesDictionaryContextBeforeTranscript() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = CleanupPromptFactory.defaultCleanupPrompt,
            rawTranscript = "請幫我寄給 open ai 的 Sarah",
            dictionaryEntries = listOf(
                DictionaryEntry(term = "OpenAI", note = "品牌名，不要加空白", id = 1L),
                DictionaryEntry(term = "Sarah Chen", note = null, id = 2L),
            ),
        )

        assertTrue(prompt.contains("使用者字典："))
        assertTrue(prompt.contains("- OpenAI：品牌名，不要加空白"))
        assertTrue(prompt.contains("- Sarah Chen"))
        assertTrue(prompt.indexOf("使用者字典：") < prompt.indexOf("原始轉錄："))
        assertTrue(prompt.contains("不要新增原文沒有的事實"))
    }

    @Test
    fun customCleanupPromptIncludesDictionaryContext() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "整理成 Slack 訊息：${CleanupPromptFactory.TranscriptPlaceholder}",
            rawTranscript = "今天要問 gemini",
            dictionaryEntries = listOf(
                DictionaryEntry(term = "Gemini", note = "Google 模型名稱", id = 1L),
            ),
        )

        assertTrue(prompt.contains("整理成 Slack 訊息：今天要問 gemini"))
        assertTrue(prompt.contains("使用者字典："))
        assertTrue(prompt.contains("- Gemini：Google 模型名稱"))
    }

    @Test
    fun cleanupPromptCapsDictionaryContext() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = CleanupPromptFactory.defaultCleanupPrompt,
            rawTranscript = "測試",
            dictionaryEntries = (1..105).map { index ->
                DictionaryEntry(term = "詞$index", note = null, id = index.toLong())
            },
        )

        assertTrue(prompt.contains("- 詞1"))
        assertTrue(prompt.contains("- 詞100"))
        assertTrue(!prompt.contains("- 詞101"))
    }
}
