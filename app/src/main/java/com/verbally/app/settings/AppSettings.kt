package com.verbally.app.settings

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
        fun fromStoredName(name: String?): AppThemeMode =
            runCatching { valueOf(name.orEmpty()) }.getOrDefault(SYSTEM)

        fun fromLabel(label: String): AppThemeMode =
            entries.firstOrNull { it.label == label } ?: SYSTEM
    }
}

data class AppSettings(
    val openAiApiKey: String = "",
    val geminiApiKey: String = "",
    val sonioxApiKey: String = "",
    val groqApiKey: String = "",
    val deepgramApiKey: String = "",
    val transcriptionProvider: TranscriptionProvider = TranscriptionProvider.OPENAI,
    val cleanupProvider: CleanupProvider = CleanupProvider.OPENAI,
    val transcriptionModel: String = "gpt-4o-mini-transcribe",
    val openAiCleanupModel: String = "gpt-5.4-nano",
    val geminiCleanupModel: String = "gemini-3.1-flash-lite",
    val cleanupPrompt: String = CleanupPromptFactory.defaultCleanupPrompt,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
)

interface SettingsRepository {
    fun load(): AppSettings
    fun save(settings: AppSettings)
    fun clearHistoryRequested()
}
