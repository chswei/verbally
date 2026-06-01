package com.verbally.app.providers

import com.verbally.app.settings.AppSettings
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
    val sonioxBaseUrl: String = "https://api.soniox.com",
)

class ProviderApiKeyTester(
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val endpoints: ProviderKeyTestEndpoints = ProviderKeyTestEndpoints(),
) : ProviderKeyTester {
    override suspend fun testTranscription(settings: AppSettings): ProviderKeyTestResult {
        val target = endpoints.transcriptionTarget(settings)
        if (target.apiKey.isBlank()) return ProviderKeyTestResult.MissingKey(target.providerName)
        return execute(target.providerName, target.toRequest())
    }

    override suspend fun testCleanup(settings: AppSettings): ProviderKeyTestResult {
        val target = endpoints.cleanupTarget(settings)
        if (target.apiKey.isBlank()) return ProviderKeyTestResult.MissingKey(target.providerName)
        return execute(target.providerName, target.toRequest())
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

}

private fun Throwable.userFacingDetail(): String =
    when (this) {
        is IOException -> message?.takeIf { it.isNotBlank() } ?: "Network error"
        else -> message?.takeIf { it.isNotBlank() } ?: this::class.java.simpleName
    }
