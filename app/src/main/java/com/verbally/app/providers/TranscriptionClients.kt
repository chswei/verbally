package com.verbally.app.providers

import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.TranscriptionProvider
import com.verbally.app.settings.transcriptionApiKey
import java.io.File

interface TranscriptionClient {
    suspend fun transcribe(apiKey: String, model: String, audioFile: File): RawTranscript
}

class TranscriptionClientRouter(
    private val openAiClient: TranscriptionClient,
    private val sonioxClient: TranscriptionClient,
    private val groqClient: TranscriptionClient,
) {
    suspend fun transcribe(settings: AppSettings, audioFile: File): RawTranscript {
        val client = when (settings.transcriptionProvider) {
            TranscriptionProvider.OPENAI -> openAiClient
            TranscriptionProvider.SONIOX -> sonioxClient
            TranscriptionProvider.GROQ -> groqClient
        }
        return client.transcribe(
            apiKey = settings.transcriptionApiKey,
            model = settings.transcriptionModel,
            audioFile = audioFile,
        )
    }

    companion object {
        fun single(client: TranscriptionClient): TranscriptionClientRouter =
            TranscriptionClientRouter(
                openAiClient = client,
                sonioxClient = client,
                groqClient = client,
            )
    }
}
