package com.verbally.app.providers

import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.settings.AppLanguage
import com.verbally.app.style.AppCategory
import com.verbally.app.style.CleanupStyleContext
import com.verbally.app.style.OutputStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanupPromptFactoryTest {
    @Test
    fun defaultCleanupPromptPreservesMixedLanguageAndForbidsTranslation() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = CleanupPromptFactory.defaultCleanupPrompt,
            rawTranscript = "我等一下要 send 給 Alex 然後 uh 補一下 deadline",
        )

        assertTrue(prompt.contains("保留原本語言"))
        assertTrue(prompt.contains("中英混用"))
        assertTrue(prompt.contains("不要翻譯"))
        assertTrue(prompt.contains("只修正明顯的語音辨識錯誤；如果不確定，保留原字。"))
        assertTrue(prompt.contains("不要潤飾成更漂亮、更正式、更可愛或更順的說法。"))
        assertTrue(prompt.contains("我等一下要 send 給 Alex"))
        assertFalse(prompt.contains("標點"))
    }

    @Test
    fun customCleanupPromptReplacesTranscriptPlaceholder() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "請整理成三點：${CleanupPromptFactory.TranscriptPlaceholder}",
            rawTranscript = "第一點 第二點",
        )

        assertTrue(prompt.contains("請整理成三點：第一點 第二點"))
        assertTrue(!prompt.contains(CleanupPromptFactory.TranscriptPlaceholder))
    }

    @Test
    fun customCleanupPromptWithoutPlaceholderAppendsTranscript() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "請整理成正式 email",
            rawTranscript = "明天 meeting 改十點",
        )

        assertTrue(prompt.contains("請整理成正式 email"))
        assertTrue(prompt.contains("原始轉錄："))
        assertTrue(prompt.contains("明天 meeting 改十點"))
    }

    @Test
    fun legacyBuiltInDefaultPromptsStillCountAsDefaults() {
        val legacyTraditionalChinesePrompt = """
            請將以下語音轉錄整理成可以直接貼到目前文字框的自然文字。

            規則：
            - 保留原本語言、語氣與中英混用比例。
            - 不要翻譯，不要把中英混用改成單一語言。
            - 去除口頭禪、重複詞與明顯語音辨識雜訊。
            - 修正常見錯字。
            - 不要新增原文沒有的事實。
            - 只輸出整理後文字，不要加說明。

            原始轉錄：
            ${CleanupPromptFactory.TranscriptPlaceholder}
        """.trimIndent()
        val legacyEnglishPrompt = """
            Please turn the following voice transcript into natural text that can be pasted directly into the current text field.

            Rules:
            - Preserve the original language, tone, and any mixed-language/code-switching ratio.
            - Do not translate, and do not convert mixed-language text into a single language.
            - Remove filler words, repeated words, and obvious speech-recognition noise.
            - Correct clear transcription mistakes.
            - Do not add facts that are not in the original transcript.
            - Output only the cleaned text, with no explanation.

            Original transcript:
            ${CleanupPromptFactory.TranscriptPlaceholder}
        """.trimIndent()

        assertTrue(CleanupPromptFactory.isBuiltInDefaultPrompt(legacyTraditionalChinesePrompt))
        assertTrue(CleanupPromptFactory.isBuiltInDefaultPrompt(legacyEnglishPrompt))
    }

    @Test
    fun customizedPromptThatMentionsLegacyDefaultTextDoesNotCountAsBuiltInDefault() {
        val customizedLegacyLikePrompt = """
            請將以下語音轉錄整理成可以直接貼到目前文字框的自然文字。

            規則：
            - 保留原本語言、語氣與中英混用比例。
            - 不要翻譯，不要把中英混用改成單一語言。
            - 去除口頭禪、重複詞與明顯語音辨識雜訊。
            - 修正常見錯字。
            - 不要新增原文沒有的事實。
            - 只輸出整理後文字，不要加說明。
            - 額外規則：請保留我的自訂格式。

            原始轉錄：
            ${CleanupPromptFactory.TranscriptPlaceholder}
        """.trimIndent()

        assertFalse(CleanupPromptFactory.isBuiltInDefaultPrompt(customizedLegacyLikePrompt))
    }

    @Test
    fun cleanupPromptIncludesDictionaryContextBeforeTranscript() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = CleanupPromptFactory.defaultCleanupPrompt,
            rawTranscript = "請幫我寄給 open ai 的 Sarah",
            dictionaryEntries = listOf(
                DictionaryEntry(term = "OpenAI", note = "品牌名，不要加空白", id = 1L),
                DictionaryEntry(term = "Sarah Chen", note = null, id = 2L),
            ),
        )

        assertTrue(prompt.contains("使用者字典："))
        assertTrue(prompt.contains("- OpenAI：品牌名，不要加空白"))
        assertTrue(prompt.contains("- Sarah Chen"))
        assertTrue(prompt.indexOf("使用者字典：") < prompt.indexOf("原始轉錄："))
        assertTrue(prompt.contains("不要新增原文沒有的事實"))
    }

    @Test
    fun cleanupPromptIncludesNoDictationSentinelInstructionBeforeTranscript() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = CleanupPromptFactory.defaultCleanupPrompt,
            rawTranscript = "鳥叫聲和背景雜音",
        )

        assertTrue(prompt.contains("<NO_DICTATION_CONTENT>"))
        assertTrue(prompt.indexOf("<NO_DICTATION_CONTENT>") < prompt.indexOf("原始轉錄："))
    }

    @Test
    fun customCleanupPromptIncludesDictionaryContext() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "整理成 Slack 訊息：${CleanupPromptFactory.TranscriptPlaceholder}",
            rawTranscript = "今天要問 gemini",
            dictionaryEntries = listOf(
                DictionaryEntry(term = "Gemini", note = "Google 模型名稱", id = 1L),
            ),
        )

        assertTrue(prompt.contains("整理成 Slack 訊息：今天要問 gemini"))
        assertTrue(prompt.contains("使用者字典："))
        assertTrue(prompt.contains("- Gemini：Google 模型名稱"))
    }

    @Test
    fun cleanupPromptCapsDictionaryContext() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = CleanupPromptFactory.defaultCleanupPrompt,
            rawTranscript = "測試",
            dictionaryEntries = (1..105).map { index ->
                DictionaryEntry(term = "詞$index", note = null, id = index.toLong())
            },
        )

        assertTrue(prompt.contains("- 詞1"))
        assertTrue(prompt.contains("- 詞100"))
        assertTrue(!prompt.contains("- 詞101"))
    }

    @Test
    fun cleanupPromptAppliesBasicTextProcessingBeforeCasualStyle() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "基本規則：移除口頭禪但保留意思。${CleanupPromptFactory.TranscriptPlaceholder}",
            rawTranscript = "我等等傳給你",
            styleContext = CleanupStyleContext(
                category = AppCategory.CHAT,
                style = OutputStyle.CASUAL,
            ),
        )

        assertTrue(prompt.contains("基本規則：移除口頭禪但保留意思。"))
        assertTrue(prompt.contains("處理順序："))
        assertTrue(prompt.contains("輸出格式只控制格式，不是重寫內容。"))
        assertTrue(prompt.contains("App 類別：聊天"))
        assertFalse(prompt.contains("以輸出語氣為準"))
        val styleBlock = prompt.styleBlock("輸出格式：Casual")
        assertEquals(
            """
                輸出格式：Casual
                Casual 規則：
                只調整標點、空格與自然斷句，讓文字看起來像聊天輸入。
                可以減少過度正式的標點，改用自然斷句、空格或換行。
                不要改中文字、英文字、數字、專有名詞。
                不要刪減內容，不要補內容，不要替換同義詞。
                不要把語氣改得更可愛、更順、更像客服或更像社群貼文。

                保護規則：
                - 內容保留優先於格式好看。
                - 如果「更自然」和「保留原字」衝突，選擇保留原字。
                - Formal/Casual 只影響格式，不影響意思、用字、資訊量與語氣強度。
                - 不要改寫、不要縮短、不要替換同義詞、不要翻譯。
                - 不要新增原文沒有的事實。
            """.trimIndent(),
            styleBlock,
        )
        assertFormatOnlyStyleBlock(styleBlock)
        assertTrue(prompt.indexOf("基本規則：") < prompt.indexOf("輸出格式：Casual"))
        assertTrue(prompt.indexOf("輸出格式：Casual") < prompt.indexOf("原始轉錄："))
    }

    @Test
    fun cleanupPromptIncludesFormalStyleInstructions() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "請修正語音辨識錯誤。",
            rawTranscript = "please send this to sarah tomorrow",
            styleContext = CleanupStyleContext(
                category = AppCategory.WORK,
                style = OutputStyle.FORMAL,
            ),
        )

        assertTrue(prompt.contains("App 類別：工作"))
        val styleBlock = prompt.styleBlock("輸出格式：Formal")
        assertEquals(
            """
                輸出格式：Formal
                Formal 規則：
                只整理標點符號、斷句、換行與必要空格。
                可以補上適合正式文字的標點，但不要換成更正式的說法。
                不要改中文字、英文字、數字、專有名詞。
                不要刪減內容，不要補內容，不要替換同義詞。
                除非是明顯語音辨識錯字，否則保留原本用詞。

                保護規則：
                - 內容保留優先於格式好看。
                - 如果「更自然」和「保留原字」衝突，選擇保留原字。
                - Formal/Casual 只影響格式，不影響意思、用字、資訊量與語氣強度。
                - 不要改寫、不要縮短、不要替換同義詞、不要翻譯。
                - 不要新增原文沒有的事實。
            """.trimIndent(),
            styleBlock,
        )
        assertFormatOnlyStyleBlock(styleBlock)
    }

    @Test
    fun cleanupPromptAppliesCustomStyleRuleAndKeepsMandatoryGuardrails() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "基本規則：移除口頭禪但保留意思。${CleanupPromptFactory.TranscriptPlaceholder}",
            rawTranscript = "我等等傳給你",
            styleContext = CleanupStyleContext(
                category = AppCategory.CHAT,
                style = OutputStyle.CASUAL,
                language = AppLanguage.TRADITIONAL_CHINESE,
                customRule = "只改標點和空格，不要改字。",
            ),
        )

        val styleBlock = prompt.styleBlock("輸出格式：Casual")
        assertTrue(styleBlock.contains("Casual 規則："))
        assertTrue(styleBlock.contains("只改標點和空格，不要改字。"))
        assertFalse(styleBlock.contains("把正式標點改成較口語的空格或斷句"))
        assertTrue(styleBlock.contains("保護規則："))
        assertFormatOnlyStyleBlock(styleBlock)
        assertTrue(prompt.indexOf("基本規則：") < prompt.indexOf("輸出格式：Casual"))
        assertTrue(prompt.indexOf("輸出格式：Casual") < prompt.indexOf("原始轉錄："))
    }

    @Test
    fun styledCustomPromptKeepsColonInstructionBeforeTranscriptPlaceholder() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = "Make this concise: ${CleanupPromptFactory.TranscriptPlaceholder}",
            rawTranscript = "send this to Sarah tomorrow",
            styleContext = CleanupStyleContext(
                category = AppCategory.WORK,
                style = OutputStyle.FORMAL,
                language = AppLanguage.ENGLISH,
            ),
        )

        val basicBlock = prompt.substringBefore("Output format: Formal")
        assertTrue(basicBlock.contains("Make this concise: the original transcript below"))
        assertFalse(basicBlock.contains("Make this concise:\n\n"))
        assertFalse(basicBlock.contains("下方原始轉錄"))
    }

    @Test
    fun styledLocalizedDefaultPromptDoesNotKeepLocalizedTranscriptFooterInBasicBlock() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.ENGLISH),
            rawTranscript = "send this to Sarah tomorrow",
            styleContext = CleanupStyleContext(
                category = AppCategory.WORK,
                style = OutputStyle.FORMAL,
                language = AppLanguage.ENGLISH,
            ),
        )

        val basicBlock = prompt.substringBefore("Output format: Formal")
        assertTrue(basicBlock.contains("Please turn the following voice transcript"))
        assertTrue(prompt.contains("Processing order:"))
        assertFalse(basicBlock.contains("Original transcript:"))
        assertFalse(basicBlock.contains(CleanupPromptFactory.TranscriptPlaceholder))
        assertTrue(prompt.contains("Output format: Formal"))
        assertTrue(prompt.contains("Protection rules:"))
        assertTrue(prompt.contains("Original transcript:\nsend this to Sarah tomorrow"))
    }

    @Test
    fun styledJapaneseDefaultPromptDoesNotKeepLocalizedTranscriptFooterInBasicBlock() {
        val prompt = CleanupPromptFactory.cleanupPrompt(
            promptTemplate = CleanupPromptFactory.defaultCleanupPromptFor(AppLanguage.JAPANESE),
            rawTranscript = "明日佐藤さんに送って",
            styleContext = CleanupStyleContext(
                category = AppCategory.WORK,
                style = OutputStyle.FORMAL,
                language = AppLanguage.JAPANESE,
            ),
        )

        val basicBlock = prompt.substringBefore("出力形式：Formal")
        assertTrue(basicBlock.contains("次の音声文字起こし"))
        assertTrue(prompt.contains("処理順序："))
        assertFalse(basicBlock.contains("元の文字起こし："))
        assertFalse(basicBlock.contains(CleanupPromptFactory.TranscriptPlaceholder))
        assertTrue(prompt.contains("元の文字起こし：\n明日佐藤さんに送って"))
    }

    private fun assertFormatOnlyStyleBlock(styleBlock: String) {
        assertTrue(styleBlock.contains("內容保留優先於格式好看"))
        assertTrue(styleBlock.contains("保留原字"))
        assertTrue(styleBlock.contains("Formal/Casual 只影響格式"))
        assertTrue(styleBlock.contains("不要改寫"))
        assertTrue(styleBlock.contains("不要縮短"))
        assertTrue(styleBlock.contains("不要替換同義詞"))
        assertTrue(styleBlock.contains("不要翻譯"))
        assertFalse(styleBlock.contains("潤飾成"))
        assertFalse(styleBlock.contains("改成更自然"))
    }

    private fun String.styleBlock(label: String): String =
        (label + substringAfter(label).substringBefore("\n\n原始轉錄：")).trim()
}
