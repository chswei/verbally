package com.verbally.app.providers

import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.TranscriptionProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

sealed class ProviderKeyTestResult {
    abstract val provider: String

    data class Success(override val provider: String) : ProviderKeyTestResult()
    data class MissingKey(override val provider: String) : ProviderKeyTestResult()
    data class Failure(override val provider: String, val detail: String) : ProviderKeyTestResult()
}

interface ProviderKeyTester {
    suspend fun testTranscription(settings: AppSettings): ProviderKeyTestResult
    suspend fun testCleanup(settings: AppSettings): ProviderKeyTestResult
}

data class ProviderKeyTestEndpoints(
    val openAiBaseUrl: String = "https://api.openai.com",
    val geminiBaseUrl: String = "https://generativelanguage.googleapis.com",
    val groqBaseUrl: String = "https://api.groq.com/openai",
    val deepgramBaseUrl: String = "https://api.deepgram.com",
    val sonioxBaseUrl: String = "https://api.soniox.com",
)

class ProviderApiKeyTester(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val endpoints: ProviderKeyTestEndpoints = ProviderKeyTestEndpoints(),
) : ProviderKeyTester {
    override suspend fun testTranscription(settings: AppSettings): ProviderKeyTestResult {
        val provider = settings.transcriptionProvider
        val providerName = provider.displayName
        val apiKey = settings.transcriptionApiKey
        if (apiKey.isBlank()) return ProviderKeyTestResult.MissingKey(providerName)

        val request = when (provider) {
            TranscriptionProvider.OPENAI -> bearerRequest(
                baseUrl = endpoints.openAiBaseUrl,
                path = "/v1/models/${settings.transcriptionModel}",
                apiKey = apiKey,
            )
            TranscriptionProvider.SONIOX -> bearerRequest(
                baseUrl = endpoints.sonioxBaseUrl,
                path = "/v1/models",
                apiKey = apiKey,
            )
            TranscriptionProvider.GROQ -> bearerRequest(
                baseUrl = endpoints.groqBaseUrl,
                path = "/v1/models/${settings.transcriptionModel}",
                apiKey = apiKey,
            )
            TranscriptionProvider.DEEPGRAM -> tokenRequest(
                baseUrl = endpoints.deepgramBaseUrl,
                path = "/v1/projects",
                apiKey = apiKey,
            )
        }
        return execute(providerName, request)
    }

    override suspend fun testCleanup(settings: AppSettings): ProviderKeyTestResult {
        val provider = settings.cleanupProvider
        val providerName = provider.displayName
        val request = when (provider) {
            CleanupProvider.OPENAI -> {
                if (settings.openAiApiKey.isBlank()) return ProviderKeyTestResult.MissingKey(providerName)
                bearerRequest(
                    baseUrl = endpoints.openAiBaseUrl,
                    path = "/v1/models/${settings.openAiCleanupModel}",
                    apiKey = settings.openAiApiKey,
                )
            }
            CleanupProvider.GEMINI -> {
                if (settings.geminiApiKey.isBlank()) return ProviderKeyTestResult.MissingKey(providerName)
                apiKeyHeaderRequest(
                    baseUrl = endpoints.geminiBaseUrl,
                    path = "/v1beta/models/${settings.geminiCleanupModel}",
                    apiKey = settings.geminiApiKey,
                )
            }
        }
        return execute(providerName, request)
    }

    private suspend fun execute(provider: String, request: Request): ProviderKeyTestResult =
        withContext(Dispatchers.IO) {
            runCatching {
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        ProviderKeyTestResult.Success(provider)
                    } else {
                        ProviderKeyTestResult.Failure(provider, "HTTP ${response.code}")
                    }
                }
            }.getOrElse { error ->
                ProviderKeyTestResult.Failure(provider, error.userFacingDetail())
            }
        }

    private fun bearerRequest(baseUrl: String, path: String, apiKey: String): Request =
        Request.Builder()
            .url(baseUrl.joinPath(path))
            .header("Authorization", "Bearer $apiKey")
            .get()
            .build()

    private fun tokenRequest(baseUrl: String, path: String, apiKey: String): Request =
        Request.Builder()
            .url(baseUrl.joinPath(path))
            .header("Authorization", "Token $apiKey")
            .get()
            .build()

    private fun apiKeyHeaderRequest(baseUrl: String, path: String, apiKey: String): Request =
        Request.Builder()
            .url(baseUrl.joinPath(path))
            .header("x-goog-api-key", apiKey)
            .get()
            .build()
}

private val TranscriptionProvider.displayName: String
    get() = when (this) {
        TranscriptionProvider.OPENAI -> "OpenAI"
        TranscriptionProvider.SONIOX -> "Soniox"
        TranscriptionProvider.GROQ -> "Groq"
        TranscriptionProvider.DEEPGRAM -> "Deepgram"
    }

private val CleanupProvider.displayName: String
    get() = when (this) {
        CleanupProvider.OPENAI -> "OpenAI"
        CleanupProvider.GEMINI -> "Gemini"
    }

private val AppSettings.transcriptionApiKey: String
    get() = when (transcriptionProvider) {
        TranscriptionProvider.OPENAI -> openAiApiKey
        TranscriptionProvider.SONIOX -> sonioxApiKey
        TranscriptionProvider.GROQ -> groqApiKey
        TranscriptionProvider.DEEPGRAM -> deepgramApiKey
    }

private fun String.joinPath(path: String): String =
    trimEnd('/') + "/" + path.trimStart('/')

private fun Throwable.userFacingDetail(): String =
    when (this) {
        is IOException -> message?.takeIf { it.isNotBlank() } ?: "Network error"
        else -> message?.takeIf { it.isNotBlank() } ?: this::class.java.simpleName
    }
