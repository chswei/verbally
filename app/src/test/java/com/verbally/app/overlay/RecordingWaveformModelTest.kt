package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingWaveformModelTest {
    @Test
    fun `uses fewer wider Wispr-like bars`() {
        val marks = RecordingWaveformModel.marks(level = 0.55f, phase = 0.25f)

        assertEquals(9, marks.size)
        assertTrue(marks.all { it.widthScale in 0.30f..0.36f })
    }

    @Test
    fun `keeps waveform away from capsule edges`() {
        assertEquals(0.20f, RecordingWaveformModel.HORIZONTAL_INSET_FRACTION, 0f)
    }

    @Test
    fun `keeps live amplitude visually restrained`() {
        val quiet = RecordingWaveformModel.marks(level = 0.05f, phase = 0f)
        val loud = RecordingWaveformModel.marks(level = 1f, phase = 0f)

        assertTrue(loud.maxOf { it.heightScale } <= 0.72f)
        assertTrue(loud.maxOf { it.heightScale } > quiet.maxOf { it.heightScale })
        assertTrue(quiet.minOf { it.heightScale } >= 0.06f)
    }
}
