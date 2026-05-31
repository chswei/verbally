package com.verbally.app.providers

import okhttp3.MultipartBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OpenAiTranscriptionRequestFactoryTest {
    @Test
    fun createsMultipartTranscriptionRequestWithBearerKeyAndModel() {
        val audio = File.createTempFile("verbally-test", ".wav").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
            deleteOnExit()
        }

        val request = OpenAiTranscriptionRequestFactory("https://api.openai.com")
            .create(
                apiKey = "sk-test",
                model = "gpt-4o-transcribe",
                audioFile = audio,
            )

        assertEquals("POST", request.method)
        assertEquals("https://api.openai.com/v1/audio/transcriptions", request.url.toString())
        assertEquals("Bearer sk-test", request.header("Authorization"))
        assertTrue(request.body is MultipartBody)

        val bodyText = Buffer().also { request.body!!.writeTo(it) }.readUtf8()
        assertTrue(bodyText.contains("name=\"model\""))
        assertTrue(bodyText.contains("gpt-4o-transcribe"))
        assertTrue(bodyText.contains("name=\"file\""))
        assertTrue(bodyText.contains("verbally-test"))
        assertTrue(bodyText.contains("name=\"chunking_strategy\""))
        assertTrue(bodyText.contains("auto"))
    }
}
