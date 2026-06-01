package com.verbally.app.settings

val TranscriptionProvider.displayName: String
    get() = when (this) {
        TranscriptionProvider.OPENAI -> "OpenAI"
        TranscriptionProvider.SONIOX -> "Soniox"
        TranscriptionProvider.GROQ -> "Groq"
    }

val CleanupProvider.displayName: String
    get() = when (this) {
        CleanupProvider.OPENAI -> "OpenAI"
        CleanupProvider.GEMINI -> "Gemini"
    }

val AppSettings.transcriptionApiKey: String
    get() = when (transcriptionProvider) {
        TranscriptionProvider.OPENAI -> openAiApiKey
        TranscriptionProvider.SONIOX -> sonioxApiKey
        TranscriptionProvider.GROQ -> groqApiKey
    }

val AppSettings.cleanupApiKey: String
    get() = when (cleanupProvider) {
        CleanupProvider.OPENAI -> openAiApiKey
        CleanupProvider.GEMINI -> geminiApiKey
    }
