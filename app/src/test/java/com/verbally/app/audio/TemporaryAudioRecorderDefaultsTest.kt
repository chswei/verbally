package com.verbally.app.audio

import android.media.MediaRecorder
import org.junit.Assert.assertEquals
import org.junit.Test

class TemporaryAudioRecorderDefaultsTest {
    @Test
    fun defaultAudioSourceIsTunedForSpeechRecognition() {
        assertEquals(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            TemporaryAudioRecorderDefaults.SPEECH_RECOGNITION_AUDIO_SOURCE,
        )
    }
}
