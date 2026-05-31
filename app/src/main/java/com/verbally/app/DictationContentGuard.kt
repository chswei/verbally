package com.verbally.app

import java.util.Locale

internal object DictationContentGuard {
    const val NoDictationSentinel: String = "<NO_DICTATION_CONTENT>"

    private const val ShortSystemMessageLimit = 80

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

    fun cleanedTextHasNoContent(text: String): Boolean {
        val compact = compact(text)
        if (compact.isBlank() || compact == sentinelCompact) return true
        if (compact in cleanedNoContentMessages) return true
        if (compact in knownNoSpeechHallucinations) return true
        if (compact.startsWith("subtitlesby") && compact.length <= ShortSystemMessageLimit) return true
        return false
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
