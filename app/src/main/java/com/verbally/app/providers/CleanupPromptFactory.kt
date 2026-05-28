package com.verbally.app.providers

import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.style.CleanupStyleContext
import com.verbally.app.style.OutputStyle

object CleanupPromptFactory {
    const val TranscriptPlaceholder = "{{transcript}}"
    private const val DictionaryContextLimit = 100

    val defaultCleanupPrompt: String = """
        請將以下語音轉錄整理成可以直接貼到目前文字框的自然文字。

        規則：
        - 保留原本語言、語氣與中英混用比例。
        - 不要翻譯，不要把中英混用改成單一語言。
        - 去除口頭禪、重複詞與明顯語音辨識雜訊。
        - 修正常見錯字。
        - 不要新增原文沒有的事實。
        - 只輸出整理後文字，不要加說明。

        原始轉錄：
        $TranscriptPlaceholder
    """.trimIndent()

    fun naturalCleanupPrompt(rawTranscript: String): String =
        cleanupPrompt(defaultCleanupPrompt, rawTranscript)

    fun cleanupPrompt(
        promptTemplate: String,
        rawTranscript: String,
        dictionaryEntries: List<DictionaryEntry> = emptyList(),
        styleContext: CleanupStyleContext? = null,
    ): String {
        if (styleContext != null) {
            return styledCleanupPrompt(promptTemplate, rawTranscript, dictionaryEntries, styleContext)
        }
        val template = promptTemplate.trim().ifBlank { defaultCleanupPrompt }
        val prompt = if (template.contains(TranscriptPlaceholder)) {
            template.replace(TranscriptPlaceholder, rawTranscript)
        } else {
            """
                $template

                原始轉錄：
                $rawTranscript
            """.trimIndent()
        }
        val dictionaryContext = dictionaryContext(dictionaryEntries)
        if (dictionaryContext.isBlank()) return prompt

        val transcriptLabelIndex = prompt.indexOf("原始轉錄：")
        return if (transcriptLabelIndex >= 0) {
            buildString {
                append(prompt.substring(0, transcriptLabelIndex).trimEnd())
                append("\n\n")
                append(dictionaryContext)
                append("\n\n")
                append(prompt.substring(transcriptLabelIndex))
            }
        } else {
            """
                $prompt

                $dictionaryContext
            """.trimIndent()
        }
    }

    private fun styledCleanupPrompt(
        promptTemplate: String,
        rawTranscript: String,
        dictionaryEntries: List<DictionaryEntry>,
        styleContext: CleanupStyleContext,
    ): String {
        val basicPrompt = basicPromptInstructions(promptTemplate)
        val dictionaryContext = dictionaryContext(dictionaryEntries)
        return buildString {
            appendLine("基本文字處理提示詞：")
            appendLine(basicPrompt)
            appendLine()
            if (dictionaryContext.isNotBlank()) {
                appendLine(dictionaryContext)
                appendLine()
            }
            appendLine("App 類別：${styleContext.category.label}")
            appendLine(styleInstructions(styleContext.style))
            appendLine()
            appendLine("原始轉錄：")
            append(rawTranscript)
        }
    }

    private fun basicPromptInstructions(promptTemplate: String): String {
        val template = promptTemplate.trim().ifBlank { defaultCleanupPrompt }
        val withoutDefaultTranscriptBlock = template.replace(
            Regex("\\n*原始轉錄：\\s*\\n\\s*${Regex.escape(TranscriptPlaceholder)}\\s*$"),
            "",
        )
        return withoutDefaultTranscriptBlock
            .replace(TranscriptPlaceholder, "下方原始轉錄")
            .trim()
    }

    private fun styleInstructions(style: OutputStyle): String = when (style) {
        OutputStyle.FORMAL -> """
            輸出語氣：Formal
            - 補上標點符號。
        """.trimIndent()
        OutputStyle.CASUAL -> """
            輸出語氣：Casual
            - 以空格取代標點符號。
        """.trimIndent()
    }

    private fun dictionaryContext(entries: List<DictionaryEntry>): String {
        val lines = entries
            .mapNotNull { entry ->
                val term = entry.term.trim()
                if (term.isBlank()) {
                    null
                } else {
                    val note = entry.note?.trim().orEmpty()
                    if (note.isBlank()) "- $term" else "- $term：$note"
                }
            }
            .take(DictionaryContextLimit)

        if (lines.isEmpty()) return ""
        return buildString {
            appendLine("使用者字典：")
            appendLine("優先保留以下詞彙的寫法；只有在原始轉錄語意相關時使用，不要新增原文沒有的內容。")
            lines.forEach { appendLine(it) }
        }.trimEnd()
    }
}
