package com.verbally.app.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedSettingsRepository(
    context: Context,
) : SettingsRepository {
    private val prefs: SharedPreferences = encryptedPrefsOrFallback(context.applicationContext)

    override fun load(): AppSettings = AppSettings(
        openAiApiKey = prefs.getString(KEY_OPENAI, "").orEmpty(),
        geminiApiKey = prefs.getString(KEY_GEMINI, "").orEmpty(),
        cleanupProvider = runCatching {
            CleanupProvider.valueOf(prefs.getString(KEY_PROVIDER, CleanupProvider.OPENAI.name).orEmpty())
        }.getOrDefault(CleanupProvider.OPENAI),
        transcriptionModel = prefs.getString(KEY_TRANSCRIPTION_MODEL, "gpt-4o-transcribe").orEmpty(),
        openAiCleanupModel = prefs.getString(KEY_OPENAI_MODEL, "gpt-5.4-nano").orEmpty(),
        geminiCleanupModel = prefs.getString(KEY_GEMINI_MODEL, "gemini-3.1-flash-lite").orEmpty(),
    )

    override fun save(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_OPENAI, settings.openAiApiKey)
            .putString(KEY_GEMINI, settings.geminiApiKey)
            .putString(KEY_PROVIDER, settings.cleanupProvider.name)
            .putString(KEY_TRANSCRIPTION_MODEL, settings.transcriptionModel)
            .putString(KEY_OPENAI_MODEL, settings.openAiCleanupModel)
            .putString(KEY_GEMINI_MODEL, settings.geminiCleanupModel)
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
        const val KEY_PROVIDER = "cleanup_provider"
        const val KEY_TRANSCRIPTION_MODEL = "transcription_model"
        const val KEY_OPENAI_MODEL = "openai_cleanup_model"
        const val KEY_GEMINI_MODEL = "gemini_cleanup_model"
    }
}
