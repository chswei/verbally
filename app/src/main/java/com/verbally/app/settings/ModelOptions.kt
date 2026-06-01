package com.verbally.app.settings

enum class TranscriptionProvider {
    OPENAI,
    SONIOX,
    GROQ,
}

data class TranscriptionModelOption(
    val provider: TranscriptionProvider,
    val model: String,
    val label: String,
)

data class CleanupModelOption(
    val provider: CleanupProvider,
    val model: String,
    val label: String,
)

object ModelOptions {
    val TranscriptionOptions = listOf(
        TranscriptionModelOption(
            provider = TranscriptionProvider.OPENAI,
            model = "gpt-4o-mini-transcribe",
            label = "OpenAI: gpt-4o-mini-transcribe",
        ),
        TranscriptionModelOption(
            provider = TranscriptionProvider.OPENAI,
            model = "gpt-4o-transcribe",
            label = "OpenAI: gpt-4o-transcribe",
        ),
        TranscriptionModelOption(
            provider = TranscriptionProvider.SONIOX,
            model = "stt-rt-v4",
            label = "Soniox: Soniox Realtime",
        ),
        TranscriptionModelOption(
            provider = TranscriptionProvider.GROQ,
            model = "whisper-large-v3-turbo",
            label = "Groq: whisper-large-v3-turbo",
        ),
    )

    val CleanupOptions = listOf(
        CleanupModelOption(
            provider = CleanupProvider.OPENAI,
            model = "gpt-5.4-nano",
            label = "OpenAI: gpt-5.4-nano",
        ),
        CleanupModelOption(
            provider = CleanupProvider.OPENAI,
            model = "gpt-5.4-mini",
            label = "OpenAI: gpt-5.4-mini",
        ),
        CleanupModelOption(
            provider = CleanupProvider.OPENAI,
            model = "gpt-5.5",
            label = "OpenAI: gpt-5.5",
        ),
        CleanupModelOption(
            provider = CleanupProvider.GEMINI,
            model = "gemini-3.1-flash-lite",
            label = "Gemini: gemini-3.1-flash-lite",
        ),
        CleanupModelOption(
            provider = CleanupProvider.GEMINI,
            model = "gemini-3.1-pro-preview",
            label = "Gemini: gemini-3.1-pro-preview",
        ),
    )
}

val AppSettings.transcriptionModelOptionLabel: String
    get() = ModelOptions.TranscriptionOptions.firstOrNull {
        it.provider == transcriptionProvider && it.model == transcriptionModel
    }?.label ?: ModelOptions.TranscriptionOptions.first().label

fun AppSettings.withTranscriptionModelOption(option: String): AppSettings {
    val selected = ModelOptions.TranscriptionOptions.firstOrNull { it.label == option } ?: return this
    return copy(
        transcriptionProvider = selected.provider,
        transcriptionModel = selected.model,
    )
}

val AppSettings.cleanupModelOptionLabel: String
    get() = ModelOptions.CleanupOptions.firstOrNull {
        when (it.provider) {
            CleanupProvider.OPENAI -> cleanupProvider == CleanupProvider.OPENAI && openAiCleanupModel == it.model
            CleanupProvider.GEMINI -> cleanupProvider == CleanupProvider.GEMINI && geminiCleanupModel == it.model
        }
    }?.label ?: ModelOptions.CleanupOptions.first().label

fun AppSettings.withCleanupModelOption(option: String): AppSettings {
    val selected = ModelOptions.CleanupOptions.firstOrNull { it.label == option } ?: return this
    return when (selected.provider) {
        CleanupProvider.OPENAI -> copy(
            cleanupProvider = CleanupProvider.OPENAI,
            openAiCleanupModel = selected.model,
        )
        CleanupProvider.GEMINI -> copy(
            cleanupProvider = CleanupProvider.GEMINI,
            geminiCleanupModel = selected.model,
        )
    }
}
