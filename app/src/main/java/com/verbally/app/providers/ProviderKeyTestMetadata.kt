package com.verbally.app.providers

import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.TranscriptionProvider
import com.verbally.app.settings.cleanupApiKey
import com.verbally.app.settings.displayName
import com.verbally.app.settings.transcriptionApiKey
import okhttp3.Request

internal enum class ProviderKeyAuthScheme {
    BEARER,
    GOOGLE_API_KEY,
}

internal data class ProviderKeyTestTarget(
    val providerName: String,
    val baseUrl: String,
    val path: String,
    val apiKey: String,
    val authScheme: ProviderKeyAuthScheme,
)

internal fun ProviderKeyTestEndpoints.transcriptionTarget(settings: AppSettings): ProviderKeyTestTarget {
    val provider = settings.transcriptionProvider
    return when (provider) {
        TranscriptionProvider.OPENAI -> ProviderKeyTestTarget(
            providerName = provider.displayName,
            baseUrl = openAiBaseUrl,
            path = "/v1/models/${settings.transcriptionModel}",
            apiKey = settings.transcriptionApiKey,
            authScheme = ProviderKeyAuthScheme.BEARER,
        )
        TranscriptionProvider.SONIOX -> ProviderKeyTestTarget(
            providerName = provider.displayName,
            baseUrl = sonioxBaseUrl,
            path = "/v1/models",
            apiKey = settings.transcriptionApiKey,
            authScheme = ProviderKeyAuthScheme.BEARER,
        )
        TranscriptionProvider.GROQ -> ProviderKeyTestTarget(
            providerName = provider.displayName,
            baseUrl = groqBaseUrl,
            path = "/v1/models/${settings.transcriptionModel}",
            apiKey = settings.transcriptionApiKey,
            authScheme = ProviderKeyAuthScheme.BEARER,
        )
    }
}

internal fun ProviderKeyTestEndpoints.cleanupTarget(settings: AppSettings): ProviderKeyTestTarget {
    val provider = settings.cleanupProvider
    return when (provider) {
        CleanupProvider.OPENAI -> ProviderKeyTestTarget(
            providerName = provider.displayName,
            baseUrl = openAiBaseUrl,
            path = "/v1/models/${settings.openAiCleanupModel}",
            apiKey = settings.cleanupApiKey,
            authScheme = ProviderKeyAuthScheme.BEARER,
        )
        CleanupProvider.GEMINI -> ProviderKeyTestTarget(
            providerName = provider.displayName,
            baseUrl = geminiBaseUrl,
            path = "/v1beta/models/${settings.geminiCleanupModel}",
            apiKey = settings.cleanupApiKey,
            authScheme = ProviderKeyAuthScheme.GOOGLE_API_KEY,
        )
    }
}

internal fun ProviderKeyTestTarget.toRequest(): Request {
    val builder = Request.Builder()
        .url(baseUrl.joinPath(path))
        .get()
    when (authScheme) {
        ProviderKeyAuthScheme.BEARER -> builder.header("Authorization", "Bearer $apiKey")
        ProviderKeyAuthScheme.GOOGLE_API_KEY -> builder.header("x-goog-api-key", apiKey)
    }
    return builder.build()
}

private fun String.joinPath(path: String): String =
    trimEnd('/') + "/" + path.trimStart('/')
