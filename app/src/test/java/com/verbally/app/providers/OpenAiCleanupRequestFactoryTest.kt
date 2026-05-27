package com.verbally.app.providers

import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiCleanupRequestFactoryTest {
    @Test
    fun createsResponsesRequestWithDefaultPrompt() {
        val request = OpenAiCleanupRequestFactory("https://api.openai.com")
            .create(
                apiKey = "openai-test",
                model = "gpt-test",
                rawTranscript = "今天 meeting 我想 follow up 這件事",
            )

        assertEquals("POST", request.method)
        assertEquals("https://api.openai.com/v1/responses", request.url.toString())
        assertEquals("Bearer openai-test", request.header("Authorization"))

        val bodyText = Buffer().also { request.body!!.writeTo(it) }.readUtf8()
        assertTrue(bodyText.contains("\"model\": \"gpt-test\""))
        assertTrue(bodyText.contains("今天 meeting 我想 follow up 這件事"))
        assertTrue(bodyText.contains("不要翻譯"))
    }

    @Test
    fun usesCustomCleanupPrompt() {
        val request = OpenAiCleanupRequestFactory("https://api.openai.com")
            .create(
                apiKey = "openai-test",
                model = "gpt-test",
                rawTranscript = "今天 meeting 我想 follow up 這件事",
                cleanupPrompt = "整理成 Slack 訊息：${CleanupPromptFactory.TranscriptPlaceholder}",
            )

        val bodyText = Buffer().also { request.body!!.writeTo(it) }.readUtf8()
        assertTrue(bodyText.contains("整理成 Slack 訊息：今天 meeting 我想 follow up 這件事"))
        assertTrue(!bodyText.contains("不要翻譯"))
    }
}
