package com.verbally.app.providers

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
}
