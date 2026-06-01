package com.verbally.app.settings

import com.verbally.app.history.HistoryRetentionMode
import com.verbally.app.providers.CleanupPromptFactory

enum class CleanupProvider {
    OPENAI,
    GEMINI,
}

enum class AppThemeMode(val label: String) {
    SYSTEM("跟隨系統"),
    LIGHT("淺色"),
    DARK("深色");

    companion object {
        fun fromLabel(label: String): AppThemeMode =
            entries.firstOrNull { it.label == label } ?: SYSTEM
    }
}

enum class AppLanguage(val label: String, val languageTag: String) {
    SYSTEM("跟隨系統", ""),
    TRADITIONAL_CHINESE("繁體中文", "zh-TW"),
    ENGLISH("English", "en"),
    SPANISH("Español", "es"),
    FRENCH("Français", "fr"),
    GERMAN("Deutsch", "de"),
    ITALIAN("Italiano", "it"),
    PORTUGUESE_BRAZIL("Português (Brasil)", "pt-BR"),
    JAPANESE("日本語", "ja"),
    KOREAN("한국어", "ko"),
    SIMPLIFIED_CHINESE("简体中文", "zh-CN");

    companion object {
        fun fromLabel(label: String): AppLanguage =
            entries.firstOrNull { it.label == label } ?: SYSTEM

        fun fromLanguageTag(languageTag: String?): AppLanguage {
            val tag = languageTag.orEmpty()
            val language = tag.substringBefore("-").lowercase()
            val region = tag.substringAfter("-", "").uppercase()
            return when (language) {
                "zh" -> if (region == "CN" || region == "SG") SIMPLIFIED_CHINESE else TRADITIONAL_CHINESE
                "en" -> ENGLISH
                "es" -> SPANISH
                "fr" -> FRENCH
                "de" -> GERMAN
                "it" -> ITALIAN
                "pt" -> PORTUGUESE_BRAZIL
                "ja" -> JAPANESE
                "ko" -> KOREAN
                else -> SYSTEM
            }
        }

        fun defaultPromptLanguageFor(
            selectedInterfaceLanguage: AppLanguage,
            systemLanguageTag: String?,
        ): AppLanguage =
            if (selectedInterfaceLanguage != SYSTEM) {
                selectedInterfaceLanguage
            } else {
                fromLanguageTag(systemLanguageTag).takeIf { it != SYSTEM } ?: ENGLISH
            }
    }
}

data class AppSettings(
    val openAiApiKey: String = "",
    val geminiApiKey: String = "",
    val sonioxApiKey: String = "",
    val groqApiKey: String = "",
    val transcriptionProvider: TranscriptionProvider = TranscriptionProvider.OPENAI,
    val cleanupProvider: CleanupProvider = CleanupProvider.OPENAI,
    val transcriptionModel: String = "gpt-4o-mini-transcribe",
    val openAiCleanupModel: String = "gpt-5.4-nano",
    val geminiCleanupModel: String = "gemini-3.1-flash-lite",
    val cleanupPrompt: String = CleanupPromptFactory.defaultCleanupPrompt,
    val cleanupPromptIsCustom: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val interfaceLanguage: AppLanguage = AppLanguage.SYSTEM,
    val historyRetentionMode: HistoryRetentionMode = HistoryRetentionMode.LATEST_100,
)

fun AppSettings.cleanupPromptForDisplay(): String =
    if (cleanupPromptIsCustom) {
        cleanupPrompt
    } else if (interfaceLanguage == AppLanguage.SYSTEM) {
        cleanupPrompt
    } else {
        CleanupPromptFactory.defaultCleanupPromptFor(interfaceLanguage)
    }

fun AppSettings.withCleanupPromptEdited(
    prompt: String,
    defaultPromptLanguage: AppLanguage = interfaceLanguage,
): AppSettings {
    val isCustom = !CleanupPromptFactory.isBuiltInDefaultPrompt(prompt)
    return copy(
        cleanupPrompt = if (isCustom) prompt else CleanupPromptFactory.defaultCleanupPromptFor(defaultPromptLanguage),
        cleanupPromptIsCustom = isCustom,
    )
}

fun AppSettings.withDefaultCleanupPromptRestored(defaultPromptLanguage: AppLanguage = interfaceLanguage): AppSettings =
    copy(
        cleanupPrompt = CleanupPromptFactory.defaultCleanupPromptFor(defaultPromptLanguage),
        cleanupPromptIsCustom = false,
    )

fun AppSettings.withInterfaceLanguage(language: AppLanguage): AppSettings =
    copy(
        interfaceLanguage = language,
        cleanupPrompt = CleanupPromptFactory.defaultCleanupPromptFor(language),
        cleanupPromptIsCustom = false,
    )

fun AppSettings.withDefaultCleanupPromptLanguage(language: AppLanguage): AppSettings =
    if (cleanupPromptIsCustom) {
        this
    } else {
        copy(cleanupPrompt = CleanupPromptFactory.defaultCleanupPromptFor(language))
    }

interface SettingsRepository {
    fun load(): AppSettings
    fun save(settings: AppSettings)
}
