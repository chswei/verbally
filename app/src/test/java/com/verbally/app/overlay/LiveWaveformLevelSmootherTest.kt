package com.verbally.app.overlay

import org.junit.Assert.assertTrue
import org.junit.Test

class LiveWaveformLevelSmootherTest {
    @Test
    fun louderAmplitudeProducesLargerVisualLevel() {
        val smoother = LiveWaveformLevelSmoother()

        val quiet = smoother.update(1_500)
        val loud = smoother.update(26_000)

        assertTrue(loud > quiet)
        assertTrue(loud <= 1f)
    }

    @Test
    fun silenceCanSettleToFlatLevel() {
        val smoother = LiveWaveformLevelSmoother()

        val level = smoother.update(0)

        assertTrue(level >= 0f)
        assertTrue(level <= 0.02f)
    }

    @Test
    fun levelsDecayGraduallyInsteadOfDroppingImmediately() {
        val smoother = LiveWaveformLevelSmoother()

        val loud = smoother.update(30_000)
        val afterDrop = smoother.update(0)

        assertTrue(afterDrop < loud)
        assertTrue(afterDrop > 0.1f)
    }
}
