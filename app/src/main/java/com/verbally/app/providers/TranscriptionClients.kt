package com.verbally.app.providers

import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.TranscriptionProvider
import java.io.File

interface TranscriptionClient {
    suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript
}

class TranscriptionClientRouter(
    private val openAiClient: TranscriptionClient,
    private val sonioxClient: TranscriptionClient,
    private val groqClient: TranscriptionClient,
    private val deepgramClient: TranscriptionClient,
) {
    suspend fun transcribe(settings: AppSettings, audioFile: File): RawTranscript {
        val client = when (settings.transcriptionProvider) {
            TranscriptionProvider.OPENAI -> openAiClient
            TranscriptionProvider.SONIOX -> sonioxClient
            TranscriptionProvider.GROQ -> groqClient
            TranscriptionProvider.DEEPGRAM -> deepgramClient
        }
        return client.transcribe(
            apiKey = settings.transcriptionApiKey,
            model = settings.transcriptionModel,
            audioFile = audioFile,
        )
    }

    private val AppSettings.transcriptionApiKey: String
        get() = when (transcriptionProvider) {
            TranscriptionProvider.OPENAI -> openAiApiKey
            TranscriptionProvider.SONIOX -> sonioxApiKey
            TranscriptionProvider.GROQ -> groqApiKey
            TranscriptionProvider.DEEPGRAM -> deepgramApiKey
        }

    companion object {
        fun single(client: TranscriptionClient): TranscriptionClientRouter =
            TranscriptionClientRouter(
                openAiClient = client,
                sonioxClient = client,
                groqClient = client,
                deepgramClient = client,
            )
    }
}
