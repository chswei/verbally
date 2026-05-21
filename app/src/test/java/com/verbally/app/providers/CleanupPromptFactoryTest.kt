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
}
