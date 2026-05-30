package com.verbally.app.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.verbally.app.providers.CleanupPromptFactory

class EncryptedSettingsRepository(
    context: Context,
) : SettingsRepository {
    private val prefs: SharedPreferences = encryptedPrefsOrFallback(context.applicationContext)

    override fun load(): AppSettings {
        val cleanupPrompt = prefs.getString(KEY_CLEANUP_PROMPT, CleanupPromptFactory.defaultCleanupPrompt)
            .orEmpty()
            .ifBlank { CleanupPromptFactory.defaultCleanupPrompt }
        val promptIsCustom = prefs.getBoolean(
            KEY_CLEANUP_PROMPT_CUSTOM,
            !CleanupPromptFactory.isBuiltInDefaultPrompt(cleanupPrompt),
        )
        return AppSettings(
            openAiApiKey = prefs.getString(KEY_OPENAI, "").orEmpty(),
            geminiApiKey = prefs.getString(KEY_GEMINI, "").orEmpty(),
            sonioxApiKey = prefs.getString(KEY_SONIOX, "").orEmpty(),
            groqApiKey = prefs.getString(KEY_GROQ, "").orEmpty(),
            deepgramApiKey = prefs.getString(KEY_DEEPGRAM, "").orEmpty(),
            transcriptionProvider = runCatching {
                TranscriptionProvider.valueOf(
                    prefs.getString(KEY_TRANSCRIPTION_PROVIDER, TranscriptionProvider.OPENAI.name).orEmpty(),
                )
            }.getOrDefault(TranscriptionProvider.OPENAI),
            cleanupProvider = runCatching {
                CleanupProvider.valueOf(prefs.getString(KEY_PROVIDER, CleanupProvider.OPENAI.name).orEmpty())
            }.getOrDefault(CleanupProvider.OPENAI),
            transcriptionModel = prefs.getString(KEY_TRANSCRIPTION_MODEL, "gpt-4o-mini-transcribe").orEmpty(),
            openAiCleanupModel = prefs.getString(KEY_OPENAI_MODEL, "gpt-5.4-nano").orEmpty(),
            geminiCleanupModel = prefs.getString(KEY_GEMINI_MODEL, "gemini-3.1-flash-lite").orEmpty(),
            cleanupPrompt = cleanupPrompt,
            cleanupPromptIsCustom = promptIsCustom,
            themeMode = AppThemeMode.fromStoredName(prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)),
            interfaceLanguage = AppLanguage.fromStoredName(
                prefs.getString(KEY_INTERFACE_LANGUAGE, AppLanguage.SYSTEM.name),
            ),
        ).normalizedModelChoices()
    }

    override fun save(settings: AppSettings) {
        val promptForStorage = settings.cleanupPrompt.ifBlank {
            CleanupPromptFactory.defaultCleanupPromptFor(settings.interfaceLanguage)
        }
        val promptIsCustom = promptForStorage.isNotBlank() &&
            settings.cleanupPromptIsCustom &&
            !CleanupPromptFactory.isBuiltInDefaultPrompt(promptForStorage)
        prefs.edit()
            .putString(KEY_OPENAI, settings.openAiApiKey)
            .putString(KEY_GEMINI, settings.geminiApiKey)
            .putString(KEY_SONIOX, settings.sonioxApiKey)
            .putString(KEY_GROQ, settings.groqApiKey)
            .putString(KEY_DEEPGRAM, settings.deepgramApiKey)
            .putString(KEY_TRANSCRIPTION_PROVIDER, settings.transcriptionProvider.name)
            .putString(KEY_PROVIDER, settings.cleanupProvider.name)
            .putString(KEY_TRANSCRIPTION_MODEL, settings.transcriptionModel)
            .putString(KEY_OPENAI_MODEL, settings.openAiCleanupModel)
            .putString(KEY_GEMINI_MODEL, settings.geminiCleanupModel)
            .putString(KEY_CLEANUP_PROMPT, promptForStorage)
            .putBoolean(KEY_CLEANUP_PROMPT_CUSTOM, promptIsCustom)
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putString(KEY_INTERFACE_LANGUAGE, settings.interfaceLanguage.name)
            .apply()
    }

    override fun clearHistoryRequested() = Unit

    private fun encryptedPrefsOrFallback(context: Context): SharedPreferences {
        return runCatching {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "verbally_secure_settings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }.getOrElse {
            context.getSharedPreferences("verbally_settings_fallback", Context.MODE_PRIVATE)
        }
    }

    private companion object {
        const val KEY_OPENAI = "openai_api_key"
        const val KEY_GEMINI = "gemini_api_key"
        const val KEY_SONIOX = "soniox_api_key"
        const val KEY_GROQ = "groq_api_key"
        const val KEY_DEEPGRAM = "deepgram_api_key"
        const val KEY_TRANSCRIPTION_PROVIDER = "transcription_provider"
        const val KEY_PROVIDER = "cleanup_provider"
        const val KEY_TRANSCRIPTION_MODEL = "transcription_model"
        const val KEY_OPENAI_MODEL = "openai_cleanup_model"
        const val KEY_GEMINI_MODEL = "gemini_cleanup_model"
        const val KEY_CLEANUP_PROMPT = "cleanup_prompt"
        const val KEY_CLEANUP_PROMPT_CUSTOM = "cleanup_prompt_custom"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_INTERFACE_LANGUAGE = "interface_language"
    }
}
