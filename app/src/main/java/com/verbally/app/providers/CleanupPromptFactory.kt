package com.verbally.app.providers

object CleanupPromptFactory {
    const val TranscriptPlaceholder = "{{transcript}}"

    val defaultCleanupPrompt: String = """
        請將以下語音轉錄整理成可以直接貼到目前文字框的自然文字。

        規則：
        - 保留原本語言、語氣與中英混用比例。
        - 不要翻譯，不要把中英混用改成單一語言。
        - 去除口頭禪、重複詞與明顯語音辨識雜訊。
        - 補上自然標點，修正常見錯字。
        - 不要新增原文沒有的事實。
        - 只輸出整理後文字，不要加說明。

        原始轉錄：
        $TranscriptPlaceholder
    """.trimIndent()

    fun naturalCleanupPrompt(rawTranscript: String): String =
        cleanupPrompt(defaultCleanupPrompt, rawTranscript)

    fun cleanupPrompt(promptTemplate: String, rawTranscript: String): String {
        val template = promptTemplate.trim().ifBlank { defaultCleanupPrompt }
        return if (template.contains(TranscriptPlaceholder)) {
            template.replace(TranscriptPlaceholder, rawTranscript)
        } else {
            """
                $template

                原始轉錄：
                $rawTranscript
            """.trimIndent()
        }
    }
}
