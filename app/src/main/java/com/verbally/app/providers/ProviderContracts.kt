package com.verbally.app.providers

import android.content.Context
import com.verbally.app.R

data class RawTranscript(
    val text: String,
    val model: String,
    val provider: String = "openai",
    val averageLogprob: Double? = null,
    val confidence: TranscriptionConfidence? = null,
    val hallucination: TranscriptionHallucination? = null,
)

enum class TranscriptionConfidence {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
}

enum class TranscriptionHallucination {
    NONE,
    SILENT,
    CRITICAL,
}

internal fun confidenceFromAverageLogprob(averageLogprob: Double): TranscriptionConfidence =
    when {
        averageLogprob <= -2.0 -> TranscriptionConfidence.NONE
        averageLogprob <= -1.0 -> TranscriptionConfidence.LOW
        averageLogprob <= -0.35 -> TranscriptionConfidence.MEDIUM
        else -> TranscriptionConfidence.HIGH
    }

internal fun noDictationRawTranscript(model: String, provider: String): RawTranscript =
    RawTranscript(
        text = "",
        model = model,
        provider = provider,
        confidence = TranscriptionConfidence.NONE,
        hallucination = TranscriptionHallucination.SILENT,
    )

data class CleanedTranscript(
    val text: String,
    val provider: String,
    val model: String,
)

class ProviderException(message: String) : RuntimeException(message)

interface ProviderMessages {
    fun missingApiKey(provider: String): String
    fun transcriptionFailed(provider: String, detail: String): String
    fun noTranscriptionText(provider: String): String
    fun responseParseFailed(provider: String): String
    fun cleanupFailed(provider: String, detail: String): String
    fun noCleanedText(provider: String): String

    companion object {
        val TraditionalChinese: ProviderMessages = object : ProviderMessages {
            override fun missingApiKey(provider: String): String = "請先在設定中填入 $provider API Key。"
            override fun transcriptionFailed(provider: String, detail: String): String = "$provider 轉錄失敗：$detail"
            override fun noTranscriptionText(provider: String): String = "$provider 沒有回傳轉錄文字。"
            override fun responseParseFailed(provider: String): String = "$provider 回傳格式無法解析。"
            override fun cleanupFailed(provider: String, detail: String): String = "$provider 文字整理失敗：$detail"
            override fun noCleanedText(provider: String): String = "$provider 沒有回傳整理後文字。"
        }
    }
}

class AndroidProviderMessages(private val context: Context) : ProviderMessages {
    override fun missingApiKey(provider: String): String =
        context.getString(R.string.provider_missing_api_key, provider)

    override fun transcriptionFailed(provider: String, detail: String): String =
        context.getString(R.string.provider_transcription_failed, provider, detail)

    override fun noTranscriptionText(provider: String): String =
        context.getString(R.string.provider_no_transcription_text, provider)

    override fun responseParseFailed(provider: String): String =
        context.getString(R.string.provider_response_parse_failed, provider)

    override fun cleanupFailed(provider: String, detail: String): String =
        context.getString(R.string.provider_cleanup_failed, provider, detail)

    override fun noCleanedText(provider: String): String =
        context.getString(R.string.provider_no_cleaned_text, provider)
}
