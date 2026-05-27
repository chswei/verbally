package com.verbally.app.providers

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
}
