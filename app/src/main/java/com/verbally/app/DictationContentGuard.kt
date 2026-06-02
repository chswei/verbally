package com.verbally.app

import com.verbally.app.providers.RawTranscript
import com.verbally.app.providers.TranscriptionConfidence
import com.verbally.app.providers.TranscriptionHallucination
import com.verbally.app.providers.confidenceFromAverageLogprob
import java.util.Locale

internal object DictationContentGuard {
    const val NoDictationSentinel: String = "<NO_DICTATION_CONTENT>"

    private const val ShortSystemMessageLimit = 80
    private const val LowConfidenceLogprobThreshold = -1.0
    private const val LowConfidenceBriefTranscriptLimit = 80

    private val removableCharacters =
        Regex("""[\s\p{Punct}，。！？、；：「」『』（）()【】\[\]《》〈〉…—－～~]+""")

    private val noSpeechSystemMessages = setOf(
        "nospeech",
        "nospeechdetected",
        "notranscription",
        "nothingtotranscribe",
        "沒有偵測到語音",
        "未偵測到語音",
        "没有检测到语音",
        "未检测到语音",
    )

    private val cleanedNoContentMessages = noSpeechSystemMessages + setOf(
        "沒有內容",
        "没有内容",
        "無內容",
        "无内容",
        "目前沒有內容",
        "目前沒有內容請輸入內容",
        "目前沒有內容請提供內容",
        "沒有可處理的內容",
        "沒有聽到語音",
        "沒有語音內容",
        "無可用內容",
        "目前没有内容",
        "目前没有内容请输入内容",
        "目前没有内容请提供内容",
        "没有可处理的内容",
        "没有听到语音",
        "没有语音内容",
        "无可用内容",
        "nocontent",
        "nocontentdetected",
        "nocontentdetectedpleaseprovidecontent",
        "nocontentdetectedpleaseentercontent",
        "thereisnocontent",
        "thereisnocontentpleaseprovidecontent",
        "thereisnocontentpleaseentercontent",
        "couldnotdetectspeech",
        "nospeechwasdetected",
        "nospeechdetectedpleaseprovidecontent",
        "nospeechdetectedpleaseentercontent",
    )

    private val knownNoSpeechHallucinations = setOf(
        "you",
        "the",
        "imgoingtogotothenextvideo",
        "thankyouforwatching",
        "thanksforwatching",
    )

    fun rawTranscriptHasNoContent(text: String): Boolean {
        val compact = compact(text)
        return compact.isBlank() ||
            compact == sentinelCompact
    }

    fun rawTranscriptHasNoContent(transcript: RawTranscript): Boolean {
        if (rawTranscriptHasNoContent(transcript.text)) return true
        if (transcript.hallucination.indicatesNoDictationContent()) return true
        if (rawTranscriptLooksLikeKnownNoSpeechHallucination(transcript.text)) return true
        if (transcript.effectiveConfidence().indicatesNoDictationContent(transcript.text)) return true
        return rawTranscriptLooksLikeLowConfidenceNoSpeech(transcript)
    }

    fun cleanedTextHasNoContent(text: String): Boolean {
        val compact = compact(text)
        if (compact.isBlank() || compact == sentinelCompact) return true
        if (compact in cleanedNoContentMessages) return true
        if (compact in knownNoSpeechHallucinations) return true
        if (compact.startsWith("subtitlesby") && compact.length <= ShortSystemMessageLimit) return true
        return false
    }

    fun cleanedTextShouldFallBackToRawTranscript(rawText: String, cleanedText: String): Boolean {
        val rawCompact = compact(rawText)
        val cleanedCompact = compact(cleanedText)
        if (rawCompact.isBlank() || cleanedCompact.isBlank()) return false
        if (rawCompact == sentinelCompact || cleanedCompact == sentinelCompact) return false
        if (rawCompact == cleanedCompact) return false

        return cleanedTextRequestsMissingOriginalContent(cleanedCompact) ||
            cleanedTextLooksLikeAssistantMetaResponse(cleanedCompact)
    }

    private fun cleanedTextRequestsMissingOriginalContent(compact: String): Boolean {
        if (compact.isBlank()) return false

        val asksToProvideChinese = compact.startsWith("請提供") || compact.startsWith("请提供")
        if (asksToProvideChinese) {
            val mentionsOriginalTranscript = compact.contains("原始轉錄") ||
                compact.contains("原始转录") ||
                compact.contains("原始轉入") ||
                compact.contains("原始转入")
            val mentionsContent = compact.contains("內容") ||
                compact.contains("内容") ||
                compact.contains("文字")
            val mentionsProcessing = compact.contains("處理") ||
                compact.contains("处理") ||
                compact.contains("轉換") ||
                compact.contains("转换") ||
                compact.contains("翻譯") ||
                compact.contains("翻译")
            return mentionsOriginalTranscript || (mentionsContent && mentionsProcessing)
        }

        val asksToProvideEnglish = compact.startsWith("pleaseprovide") || compact.startsWith("provide")
        return asksToProvideEnglish &&
            (compact.contains("originaltranscript") || compact.contains("content"))
    }

    private fun cleanedTextLooksLikeAssistantMetaResponse(compact: String): Boolean {
        val acknowledgesChineseInstruction = compact.startsWith("好的我會") ||
            compact.startsWith("好的我会") ||
            compact.startsWith("我會") ||
            compact.startsWith("我会") ||
            compact.startsWith("已設定") ||
            compact.startsWith("已设置") ||
            compact.startsWith("我可以協助") ||
            compact.startsWith("我可以协助")
        if (acknowledgesChineseInstruction) {
            return compact.contains("繁體中文") ||
                compact.contains("繁体中文") ||
                compact.contains("中文") ||
                compact.contains("英文") ||
                compact.contains("翻譯") ||
                compact.contains("翻译") ||
                compact.contains("轉錄") ||
                compact.contains("转录")
        }

        val acknowledgesEnglishInstruction = compact.startsWith("sureiwill") ||
            compact.startsWith("iwill") ||
            compact.startsWith("ican") ||
            compact.startsWith("icanhelp") ||
            compact.startsWith("setto")
        return acknowledgesEnglishInstruction &&
            (compact.contains("english") || compact.contains("chinese") || compact.contains("translate"))
    }

    private fun rawTranscriptLooksLikeLowConfidenceNoSpeech(transcript: RawTranscript): Boolean {
        val averageLogprob = transcript.averageLogprob ?: return false
        if (averageLogprob > LowConfidenceLogprobThreshold) return false

        val compact = compact(transcript.text)
        if (compactLooksLikeKnownNoSpeechHallucination(compact)) return true
        return compact.length <= LowConfidenceBriefTranscriptLimit
    }

    private fun rawTranscriptLooksLikeKnownNoSpeechHallucination(text: String): Boolean =
        compactLooksLikeKnownNoSpeechHallucination(compact(text))

    private fun compactLooksLikeKnownNoSpeechHallucination(compact: String): Boolean {
        if (compact in noSpeechSystemMessages) return true
        if (compact in knownNoSpeechHallucinations) return true
        return compact.startsWith("subtitlesby") && compact.length <= ShortSystemMessageLimit
    }

    private fun RawTranscript.effectiveConfidence(): TranscriptionConfidence? =
        confidence ?: averageLogprob?.let(::confidenceFromAverageLogprob)

    private fun TranscriptionHallucination?.indicatesNoDictationContent(): Boolean =
        when (this) {
            TranscriptionHallucination.SILENT,
            TranscriptionHallucination.CRITICAL,
            -> true
            TranscriptionHallucination.NONE,
            null,
            -> false
        }

    private fun TranscriptionConfidence?.indicatesNoDictationContent(text: String): Boolean {
        val compact = compact(text)
        return when (this) {
            TranscriptionConfidence.NONE -> true
            TranscriptionConfidence.LOW -> compact.length <= LowConfidenceBriefTranscriptLimit ||
                compact in noSpeechSystemMessages ||
                compact in knownNoSpeechHallucinations
            TranscriptionConfidence.MEDIUM,
            TranscriptionConfidence.HIGH,
            null,
            -> false
        }
    }

    private val sentinelCompact: String = compact(NoDictationSentinel)

    private fun compact(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return ""
        return trimmed
            .lowercase(Locale.ROOT)
            .replace(removableCharacters, "")
    }
}
