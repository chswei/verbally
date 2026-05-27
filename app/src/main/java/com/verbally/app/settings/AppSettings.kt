package com.verbally.app.settings

import com.verbally.app.providers.CleanupPromptFactory

enum class CleanupProvider {
    OPENAI,
    GEMINI,
}

data class AppSettings(
    val openAiApiKey: String = "",
    val geminiApiKey: String = "",
    val cleanupProvider: CleanupProvider = CleanupProvider.OPENAI,
    val transcriptionModel: String = "gpt-4o-transcribe",
    val openAiCleanupModel: String = "gpt-5.4-nano",
    val geminiCleanupModel: String = "gemini-3.1-flash-lite",
    val cleanupPrompt: String = CleanupPromptFactory.defaultCleanupPrompt,
)

interface SettingsRepository {
    fun load(): AppSettings
    fun save(settings: AppSettings)
    fun clearHistoryRequested()
}
