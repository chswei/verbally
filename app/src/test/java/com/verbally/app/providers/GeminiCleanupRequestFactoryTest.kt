package com.verbally.app.providers

import com.verbally.app.dictionary.DictionaryEntry
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiCleanupRequestFactoryTest {
    @Test
    fun createsGenerateContentRequestWithGoogApiKeyHeader() {
        val request = GeminiCleanupRequestFactory("https://generativelanguage.googleapis.com")
            .create(
                apiKey = "gemini-test",
                model = "gemini-3.5-flash",
                rawTranscript = "今天 meeting 我想 follow up 這件事",
            )

        assertEquals("POST", request.method)
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent",
            request.url.toString(),
        )
        assertEquals("gemini-test", request.header("x-goog-api-key"))

        val bodyText = Buffer().also { request.body!!.writeTo(it) }.readUtf8()
        assertTrue(bodyText.contains("\"contents\""))
        assertTrue(bodyText.contains("今天 meeting 我想 follow up 這件事"))
        assertTrue(bodyText.contains("不要翻譯"))
    }

    @Test
    fun usesCustomCleanupPrompt() {
        val request = GeminiCleanupRequestFactory("https://generativelanguage.googleapis.com")
            .create(
                apiKey = "gemini-test",
                model = "gemini-3.5-flash",
                rawTranscript = "今天 meeting 我想 follow up 這件事",
                cleanupPrompt = "整理成 Slack 訊息：${CleanupPromptFactory.TranscriptPlaceholder}",
            )

        val bodyText = Buffer().also { request.body!!.writeTo(it) }.readUtf8()
        assertTrue(bodyText.contains("整理成 Slack 訊息：今天 meeting 我想 follow up 這件事"))
        assertTrue(!bodyText.contains("不要翻譯"))
    }

    @Test
    fun includesDictionaryEntriesInCleanupPrompt() {
        val request = GeminiCleanupRequestFactory("https://generativelanguage.googleapis.com")
            .create(
                apiKey = "gemini-test",
                model = "gemini-3.5-flash",
                rawTranscript = "請提醒 open ai 的 Sarah",
                dictionaryEntries = listOf(
                    DictionaryEntry(term = "OpenAI", note = "品牌名，不要加空白", id = 1L),
                    DictionaryEntry(term = "Sarah Chen", note = null, id = 2L),
                ),
            )

        val bodyText = Buffer().also { request.body!!.writeTo(it) }.readUtf8()
        assertTrue(bodyText.contains("使用者字典："))
        assertTrue(bodyText.contains("- OpenAI：品牌名，不要加空白"))
        assertTrue(bodyText.contains("- Sarah Chen"))
    }
}
