package com.verbally.app.settings

import com.verbally.app.providers.CleanupPromptFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanupPromptPreferencesTest {
    @Test
    fun defaultPromptFollowsInterfaceLanguage() {
        val englishSettings = AppSettings().withInterfaceLanguage(AppLanguage.ENGLISH)

        assertFalse(englishSettings.cleanupPromptIsCustom)
        assertEquals(
            CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
            englishSettings.cleanupPromptForDisplay(),
        )
        assertTrue(englishSettings.cleanupPromptForDisplay().contains("Do not translate"))
    }

    @Test
    fun builtInPromptDisplayFollowsInterfaceLanguageEvenWhenStoredPromptIsFromAnotherLocale() {
        val englishSettingsWithTraditionalChineseDefault = AppSettings(
            interfaceLanguage = AppLanguage.ENGLISH,
            cleanupPrompt = CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.TRADITIONAL_CHINESE),
            cleanupPromptIsCustom = false,
        )

        assertEquals(
            CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
            englishSettingsWithTraditionalChineseDefault.cleanupPromptForDisplay(),
        )
        assertTrue(englishSettingsWithTraditionalChineseDefault.cleanupPromptForDisplay().contains("Do not translate"))
        assertFalse(englishSettingsWithTraditionalChineseDefault.cleanupPromptForDisplay().contains("請將以下語音轉錄"))
    }

    @Test
    fun builtInPromptDisplayFollowsEverySupportedInterfaceLanguage() {
        val staleStoredPrompt = CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.TRADITIONAL_CHINESE)
        val supportedLanguages = AppLanguage.entries.filterNot { it == AppLanguage.SYSTEM }

        supportedLanguages.forEach { language ->
            val settings = AppSettings(
                interfaceLanguage = language,
                cleanupPrompt = staleStoredPrompt,
                cleanupPromptIsCustom = false,
            )

            assertEquals(
                "Default prompt should display in $language",
                CleanupPromptFactory.defaultCleanupPromptFor(language),
                settings.cleanupPromptForDisplay(),
            )
        }
    }

    @Test
    fun systemInterfaceLanguageDisplaysResolvedSystemDefaultPrompt() {
        val settings = AppSettings(
            interfaceLanguage = AppLanguage.SYSTEM,
            cleanupPrompt = CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
            cleanupPromptIsCustom = false,
        )

        assertEquals(
            CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
            settings.cleanupPromptForDisplay(),
        )
        assertTrue(settings.cleanupPromptForDisplay().contains("Do not translate"))
    }

    @Test
    fun customPromptSurvivesInterfaceLanguageChanges() {
        val customSettings = AppSettings()
            .withCleanupPromptEdited("整理成我自己的格式：${CleanupPromptFactory.TranscriptPlaceholder}")
            .withInterfaceLanguage(AppLanguage.JAPANESE)

        assertTrue(customSettings.cleanupPromptIsCustom)
        assertEquals(
            "整理成我自己的格式：${CleanupPromptFactory.TranscriptPlaceholder}",
            customSettings.cleanupPromptForDisplay(),
        )
    }

    @Test
    fun restoringDefaultUsesCurrentInterfaceLanguage() {
        val restoredSettings = AppSettings(interfaceLanguage = AppLanguage.JAPANESE)
            .withCleanupPromptEdited("custom")
            .withDefaultCleanupPromptRestored()

        assertFalse(restoredSettings.cleanupPromptIsCustom)
        assertEquals(
            CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.JAPANESE),
            restoredSettings.cleanupPromptForDisplay(),
        )
        assertTrue(restoredSettings.cleanupPromptForDisplay().contains("翻訳しない"))
    }

    @Test
    fun restoringDefaultCanUseResolvedSystemPromptLanguage() {
        val restoredSettings = AppSettings(interfaceLanguage = AppLanguage.SYSTEM)
            .withCleanupPromptEdited("custom")
            .withDefaultCleanupPromptRestored(AppLanguage.ENGLISH)

        assertFalse(restoredSettings.cleanupPromptIsCustom)
        assertEquals(
            CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
            restoredSettings.cleanupPromptForDisplay(),
        )
    }

    @Test
    fun editingBuiltInPromptCanUseResolvedSystemPromptLanguage() {
        val settings = AppSettings(interfaceLanguage = AppLanguage.SYSTEM)
            .withCleanupPromptEdited(
                CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
                defaultPromptLanguage = AppLanguage.ENGLISH,
            )

        assertFalse(settings.cleanupPromptIsCustom)
        assertEquals(
            CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
            settings.cleanupPromptForDisplay(),
        )
    }
}
