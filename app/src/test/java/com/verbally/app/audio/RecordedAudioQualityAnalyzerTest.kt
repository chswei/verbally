package com.verbally.app.audio

import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordedAudioQualityAnalyzerTest {
    @Test
    fun tooShortWavIsNoSpeechContentEvenWhenItHasAmplitude() {
        val file = tempWav(samples = ShortArray(16_000 / 4) { 1_800 })

        val quality = RecordedAudioQualityAnalyzer.analyze(file)

        assertTrue(quality.known)
        assertTrue(quality.clearlyHasNoSpeechContent())
    }

    @Test
    fun longerSilentWavIsNoSpeechContent() {
        val file = tempWav(samples = ShortArray(16_000) { 0 })

        val quality = RecordedAudioQualityAnalyzer.analyze(file)

        assertTrue(quality.known)
        assertTrue(quality.clearlyHasNoSpeechContent())
    }

    @Test
    fun longerWavWithMeaningfulAmplitudeIsAllowedThroughProviderGate() {
        val file = tempWav(samples = ShortArray(16_000) { index ->
            if (index % 2 == 0) 900.toShort() else (-900).toShort()
        })

        val quality = RecordedAudioQualityAnalyzer.analyze(file)

        assertTrue(quality.known)
        assertFalse(quality.clearlyHasNoSpeechContent())
    }

    private fun tempWav(samples: ShortArray): File {
        val file = File.createTempFile("verbally-quality-", ".wav")
        FileOutputStream(file).use { output ->
            WavFileWriter.writeHeader(output, dataSizeBytes = samples.size * 2L)
            samples.forEach { sample ->
                output.write(sample.toInt() and 0xff)
                output.write((sample.toInt() shr 8) and 0xff)
            }
        }
        file.deleteOnExit()
        return file
    }
}
