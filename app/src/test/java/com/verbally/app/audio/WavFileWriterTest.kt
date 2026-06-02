package com.verbally.app.audio

import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class WavFileWriterTest {
    @Test
    fun writesPcm16Mono16kHeaderAndPatchesFinalDataSize() {
        val file = File.createTempFile("verbally-test-", ".wav")
        try {
            FileOutputStream(file).use { output ->
                WavFileWriter.writeHeader(output, dataSizeBytes = 0)
                output.write(byteArrayOf(1, 2, 3, 4))
            }

            WavFileWriter.patchHeader(file, dataSizeBytes = 4)

            val bytes = file.readBytes()
            assertArrayEquals(byteArrayOf('R'.code.toByte(), 'I'.code.toByte(), 'F'.code.toByte(), 'F'.code.toByte()), bytes.sliceArray(0..3))
            assertLittleEndianInt(40, bytes, 4)
            assertArrayEquals(byteArrayOf('W'.code.toByte(), 'A'.code.toByte(), 'V'.code.toByte(), 'E'.code.toByte()), bytes.sliceArray(8..11))
            assertArrayEquals(byteArrayOf('f'.code.toByte(), 'm'.code.toByte(), 't'.code.toByte(), ' '.code.toByte()), bytes.sliceArray(12..15))
            assertLittleEndianInt(16, bytes, 16)
            assertLittleEndianShort(1, bytes, 20)
            assertLittleEndianShort(1, bytes, 22)
            assertLittleEndianInt(16_000, bytes, 24)
            assertLittleEndianInt(32_000, bytes, 28)
            assertLittleEndianShort(2, bytes, 32)
            assertLittleEndianShort(16, bytes, 34)
            assertArrayEquals(byteArrayOf('d'.code.toByte(), 'a'.code.toByte(), 't'.code.toByte(), 'a'.code.toByte()), bytes.sliceArray(36..39))
            assertLittleEndianInt(4, bytes, 40)
            assertArrayEquals(byteArrayOf(1, 2, 3, 4), bytes.sliceArray(44..47))
        } finally {
            file.delete()
        }
    }

    private fun assertLittleEndianInt(expected: Int, bytes: ByteArray, offset: Int) {
        val actual = (bytes[offset].toInt() and 0xff) or
            ((bytes[offset + 1].toInt() and 0xff) shl 8) or
            ((bytes[offset + 2].toInt() and 0xff) shl 16) or
            ((bytes[offset + 3].toInt() and 0xff) shl 24)
        assertEquals(expected, actual)
    }

    private fun assertLittleEndianShort(expected: Int, bytes: ByteArray, offset: Int) {
        val actual = (bytes[offset].toInt() and 0xff) or
            ((bytes[offset + 1].toInt() and 0xff) shl 8)
        assertEquals(expected, actual)
    }
}
