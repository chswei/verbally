package com.verbally.app.providers

import okhttp3.MultipartBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AdditionalTranscriptionRequestFactoryTest {
    @Test
    fun createsGroqWhisperRequestWithOpenAiCompatibleMultipartBody() {
        val audio = tempAudio()

        val request = GroqTranscriptionRequestFactory("https://api.groq.com/openai")
            .create(apiKey = "groq-test", model = "whisper-large-v3-turbo", audioFile = audio)

        assertEquals("POST", request.method)
        assertEquals("https://api.groq.com/openai/v1/audio/transcriptions", request.url.toString())
        assertEquals("Bearer groq-test", request.header("Authorization"))
        assertTrue(request.body is MultipartBody)

        val bodyText = Buffer().also { request.body!!.writeTo(it) }.readUtf8()
        assertTrue(bodyText.contains("name=\"model\""))
        assertTrue(bodyText.contains("whisper-large-v3-turbo"))
        assertTrue(bodyText.contains("name=\"file\""))
        assertTrue(bodyText.contains(audio.name))
        assertTrue(bodyText.contains("name=\"response_format\""))
    }

    private fun tempAudio(): File =
        File.createTempFile("verbally-test", ".m4a").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
            deleteOnExit()
        }
}
