package com.verbally.app.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun exposesSupportedInterfaceLanguagesInSettingsOrder() {
        assertEquals(
            listOf(
                "跟隨系統",
                "繁體中文",
                "English",
                "Español",
                "Français",
                "Deutsch",
                "Italiano",
                "Português (Brasil)",
                "日本語",
                "한국어",
                "简体中文",
            ),
            AppLanguage.entries.map { it.label },
        )
    }

    @Test
    fun exposesLocaleTagsForManualLanguages() {
        assertEquals("", AppLanguage.SYSTEM.languageTag)
        assertEquals("zh-TW", AppLanguage.TRADITIONAL_CHINESE.languageTag)
        assertEquals("en", AppLanguage.ENGLISH.languageTag)
        assertEquals("es", AppLanguage.SPANISH.languageTag)
        assertEquals("fr", AppLanguage.FRENCH.languageTag)
        assertEquals("de", AppLanguage.GERMAN.languageTag)
        assertEquals("it", AppLanguage.ITALIAN.languageTag)
        assertEquals("pt-BR", AppLanguage.PORTUGUESE_BRAZIL.languageTag)
        assertEquals("ja", AppLanguage.JAPANESE.languageTag)
        assertEquals("ko", AppLanguage.KOREAN.languageTag)
        assertEquals("zh-CN", AppLanguage.SIMPLIFIED_CHINESE.languageTag)
    }

    @Test
    fun appSettingsDefaultsToFollowingSystemLanguage() {
        assertEquals(AppLanguage.SYSTEM, AppSettings().interfaceLanguage)
    }

    @Test
    fun storedLanguageNamesFallBackToSystemWhenInvalid() {
        assertEquals(AppLanguage.JAPANESE, AppLanguage.fromStoredName("JAPANESE"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStoredName(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStoredName("missing"))
    }

    @Test
    fun languageLabelsFallBackToSystemWhenInvalid() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLabel("English"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLabel("missing"))
    }

    @Test
    fun languageTagsMapToSupportedManualLanguages() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLanguageTag("en-US"))
        assertEquals(AppLanguage.SPANISH, AppLanguage.fromLanguageTag("es-MX"))
        assertEquals(AppLanguage.PORTUGUESE_BRAZIL, AppLanguage.fromLanguageTag("pt-BR"))
        assertEquals(AppLanguage.TRADITIONAL_CHINESE, AppLanguage.fromLanguageTag("zh-TW"))
        assertEquals(AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.fromLanguageTag("zh-CN"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTag("nl-NL"))
    }

    @Test
    fun defaultPromptLanguageUsesManualSelectionOrSystemLocale() {
        assertEquals(
            AppLanguage.JAPANESE,
            AppLanguage.defaultPromptLanguageFor(
                selectedInterfaceLanguage = AppLanguage.JAPANESE,
                systemLanguageTag = "en-US",
            ),
        )
        assertEquals(
            AppLanguage.ENGLISH,
            AppLanguage.defaultPromptLanguageFor(
                selectedInterfaceLanguage = AppLanguage.SYSTEM,
                systemLanguageTag = "en-US",
            ),
        )
        assertEquals(
            AppLanguage.TRADITIONAL_CHINESE,
            AppLanguage.defaultPromptLanguageFor(
                selectedInterfaceLanguage = AppLanguage.SYSTEM,
                systemLanguageTag = "nl-NL",
            ),
        )
    }
}
