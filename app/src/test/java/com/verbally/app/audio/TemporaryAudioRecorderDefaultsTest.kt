package com.verbally.app.audio

import android.media.AudioFormat
import android.media.MediaRecorder
import org.junit.Assert.assertEquals
import org.junit.Test

class TemporaryAudioRecorderDefaultsTest {
    @Test
    fun defaultAudioSourceMatchesWisprLikeMicCapture() {
        assertEquals(
            MediaRecorder.AudioSource.MIC,
            TemporaryAudioRecorderDefaults.AUDIO_SOURCE,
        )
    }

    @Test
    fun defaultFormatIsSpeechFriendlyPcmWav() {
        assertEquals(16_000, TemporaryAudioRecorderDefaults.SAMPLE_RATE_HZ)
        assertEquals(AudioFormat.CHANNEL_IN_MONO, TemporaryAudioRecorderDefaults.CHANNEL_CONFIG)
        assertEquals(1, TemporaryAudioRecorderDefaults.CHANNEL_COUNT)
        assertEquals(AudioFormat.ENCODING_PCM_16BIT, TemporaryAudioRecorderDefaults.AUDIO_ENCODING)
        assertEquals(16, TemporaryAudioRecorderDefaults.BITS_PER_SAMPLE)
        assertEquals(".wav", TemporaryAudioRecorderDefaults.FILE_EXTENSION)
    }
}
