package com.verbally.app.style

import com.verbally.app.settings.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStyleRuleRepositoryTest {
    @Test
    fun customRuleIsScopedByLanguageAndOutputStyle() {
        val repository = InMemoryAppStyleRuleRepository()

        repository.saveCustomRule(
            language = AppLanguage.TRADITIONAL_CHINESE,
            style = OutputStyle.CASUAL,
            rule = "只改標點和空格，不要改字。",
        )

        val traditionalChineseCasual = repository.ruleFor(
            language = AppLanguage.TRADITIONAL_CHINESE,
            style = OutputStyle.CASUAL,
        )
        val traditionalChineseFormal = repository.ruleFor(
            language = AppLanguage.TRADITIONAL_CHINESE,
            style = OutputStyle.FORMAL,
        )
        val japaneseCasual = repository.ruleFor(
            language = AppLanguage.JAPANESE,
            style = OutputStyle.CASUAL,
        )

        assertTrue(traditionalChineseCasual.isCustom)
        assertEquals("只改標點和空格，不要改字。", traditionalChineseCasual.rule)
        assertFalse(traditionalChineseFormal.isCustom)
        assertFalse(japaneseCasual.isCustom)
        assertNull(repository.customRuleFor(AppLanguage.JAPANESE, OutputStyle.CASUAL))
    }

    @Test
    fun restoreDefaultRemovesOnlyMatchingLanguageAndStyleOverride() {
        val repository = InMemoryAppStyleRuleRepository()
        repository.saveCustomRule(AppLanguage.TRADITIONAL_CHINESE, OutputStyle.CASUAL, "繁中 Casual")
        repository.saveCustomRule(AppLanguage.ENGLISH, OutputStyle.CASUAL, "English Casual")

        repository.restoreDefault(AppLanguage.TRADITIONAL_CHINESE, OutputStyle.CASUAL)

        assertFalse(repository.ruleFor(AppLanguage.TRADITIONAL_CHINESE, OutputStyle.CASUAL).isCustom)
        assertEquals(
            "English Casual",
            repository.ruleFor(AppLanguage.ENGLISH, OutputStyle.CASUAL).rule,
        )
    }
}
