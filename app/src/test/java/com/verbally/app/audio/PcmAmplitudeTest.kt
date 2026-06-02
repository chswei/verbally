package com.verbally.app.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class PcmAmplitudeTest {
    @Test
    fun returnsLargestAbsolutePcm16Sample() {
        val buffer = byteArrayOf(
            0x00, 0x00,
            0xe8.toByte(), 0x03,
            0x18, 0xfc.toByte(),
            0xff.toByte(), 0x7f,
        )

        assertEquals(32_767, PcmAmplitude.maxAmplitude(buffer, buffer.size))
    }

    @Test
    fun ignoresTrailingPartialSample() {
        val buffer = byteArrayOf(
            0x10, 0x27,
            0x7f,
        )

        assertEquals(10_000, PcmAmplitude.maxAmplitude(buffer, buffer.size))
    }

    @Test
    fun emptyBufferHasZeroAmplitude() {
        assertEquals(0, PcmAmplitude.maxAmplitude(ByteArray(0), 0))
    }
}
