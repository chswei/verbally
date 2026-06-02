package com.verbally.app.audio

import java.io.File
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

internal data class RecordedAudioQuality(
    val known: Boolean,
    val durationMillis: Long,
    val peakAmplitude: Int,
    val rmsAmplitude: Double,
    val activeFrameRatio: Double,
    val audioSnrDb: Double?,
) {
    fun clearlyHasNoSpeechContent(): Boolean {
        if (!known) return false
        if (durationMillis == 0L) return true
        if (durationMillis < MinimumUsefulRecordingMillis) return true
        if (peakAmplitude == 0) return true
        if (peakAmplitude < NearSilencePeakAmplitude && rmsAmplitude < NearSilenceRmsAmplitude) return true
        return activeFrameRatio <= NearSilenceActiveFrameRatio &&
            peakAmplitude < LowActivityPeakAmplitude &&
            rmsAmplitude < LowActivityRmsAmplitude
    }

    companion object {
        const val MinimumUsefulRecordingMillis: Long = 500

        private const val NearSilencePeakAmplitude: Int = 16
        private const val NearSilenceRmsAmplitude: Double = 2.0
        private const val NearSilenceActiveFrameRatio: Double = 0.01
        private const val LowActivityPeakAmplitude: Int = 96
        private const val LowActivityRmsAmplitude: Double = 8.0

        val Unknown: RecordedAudioQuality = RecordedAudioQuality(
            known = false,
            durationMillis = 0,
            peakAmplitude = 0,
            rmsAmplitude = 0.0,
            activeFrameRatio = 0.0,
            audioSnrDb = null,
        )
    }
}

internal object RecordedAudioQualityAnalyzer {
    private const val HeaderSizeBytes = 44
    private const val FrameDurationMillis = 20
    private const val ActiveFrameRmsAmplitude = 80.0

    fun analyze(file: File): RecordedAudioQuality {
        val bytes = runCatching { file.readBytes() }.getOrNull() ?: return RecordedAudioQuality.Unknown
        if (bytes.size < HeaderSizeBytes) return RecordedAudioQuality.Unknown
        if (bytes.ascii(0, 4) != "RIFF" || bytes.ascii(8, 4) != "WAVE") return RecordedAudioQuality.Unknown

        val wav = parseWav(bytes) ?: return RecordedAudioQuality.Unknown
        if (wav.audioFormat != 1 || wav.bitsPerSample != 16 || wav.channels <= 0 || wav.sampleRate <= 0) {
            return RecordedAudioQuality.Unknown
        }
        val dataEnd = (wav.dataOffset + wav.dataSizeBytes).coerceAtMost(bytes.size)
        if (dataEnd <= wav.dataOffset) {
            return RecordedAudioQuality(
                known = true,
                durationMillis = 0,
                peakAmplitude = 0,
                rmsAmplitude = 0.0,
                activeFrameRatio = 0.0,
                audioSnrDb = null,
            )
        }

        val sampleCount = (dataEnd - wav.dataOffset) / TemporaryAudioRecorderDefaults.BYTES_PER_SAMPLE
        val frameCount = sampleCount / wav.channels
        val durationMillis = frameCount * 1_000L / wav.sampleRate
        val samplesPerFrame = (wav.sampleRate * FrameDurationMillis / 1_000).coerceAtLeast(1) * wav.channels
        val frameRmsValues = mutableListOf<Double>()

        var index = wav.dataOffset
        var samplesInCurrentFrame = 0
        var currentFrameSquareSum = 0.0
        var squareSum = 0.0
        var peak = 0
        var activeFrames = 0
        var totalFrames = 0

        while (index + 1 < dataEnd) {
            val sample = bytes.littleEndianShort(index)
            val amplitude = if (sample == Short.MIN_VALUE.toInt()) Short.MAX_VALUE.toInt() else abs(sample)
            if (amplitude > peak) peak = amplitude
            val square = sample.toDouble() * sample.toDouble()
            squareSum += square
            currentFrameSquareSum += square
            samplesInCurrentFrame += 1
            if (samplesInCurrentFrame >= samplesPerFrame) {
                val frameRms = sqrt(currentFrameSquareSum / samplesInCurrentFrame)
                frameRmsValues += frameRms
                if (frameRms >= ActiveFrameRmsAmplitude) activeFrames += 1
                totalFrames += 1
                samplesInCurrentFrame = 0
                currentFrameSquareSum = 0.0
            }
            index += TemporaryAudioRecorderDefaults.BYTES_PER_SAMPLE
        }

        if (samplesInCurrentFrame > 0) {
            val frameRms = sqrt(currentFrameSquareSum / samplesInCurrentFrame)
            frameRmsValues += frameRms
            if (frameRms >= ActiveFrameRmsAmplitude) activeFrames += 1
            totalFrames += 1
        }

        val rms = if (sampleCount == 0) 0.0 else sqrt(squareSum / sampleCount)
        return RecordedAudioQuality(
            known = true,
            durationMillis = durationMillis,
            peakAmplitude = peak,
            rmsAmplitude = rms,
            activeFrameRatio = if (totalFrames == 0) 0.0 else activeFrames.toDouble() / totalFrames,
            audioSnrDb = audioSnrDb(frameRmsValues),
        )
    }

    private fun parseWav(bytes: ByteArray): WavInfo? {
        var offset = 12
        var audioFormat: Int? = null
        var channels: Int? = null
        var sampleRate: Int? = null
        var bitsPerSample: Int? = null
        var dataOffset: Int? = null
        var dataSize: Int? = null

        while (offset + 8 <= bytes.size) {
            val id = bytes.ascii(offset, 4)
            val chunkSize = bytes.littleEndianInt(offset + 4).coerceAtLeast(0)
            val chunkStart = offset + 8
            val chunkEnd = (chunkStart + chunkSize).coerceAtMost(bytes.size)
            when (id) {
                "fmt " -> {
                    if (chunkEnd - chunkStart >= 16) {
                        audioFormat = bytes.littleEndianUnsignedShort(chunkStart)
                        channels = bytes.littleEndianUnsignedShort(chunkStart + 2)
                        sampleRate = bytes.littleEndianInt(chunkStart + 4)
                        bitsPerSample = bytes.littleEndianUnsignedShort(chunkStart + 14)
                    }
                }
                "data" -> {
                    dataOffset = chunkStart
                    dataSize = chunkEnd - chunkStart
                }
            }
            offset = chunkStart + chunkSize + (chunkSize and 1)
        }

        return WavInfo(
            audioFormat = audioFormat ?: return null,
            channels = channels ?: return null,
            sampleRate = sampleRate ?: return null,
            bitsPerSample = bitsPerSample ?: return null,
            dataOffset = dataOffset ?: return null,
            dataSizeBytes = dataSize ?: return null,
        )
    }

    private fun audioSnrDb(frameRmsValues: List<Double>): Double? {
        if (frameRmsValues.size < 2) return null
        val sorted = frameRmsValues.sorted()
        val quiet = sorted[(sorted.lastIndex * 0.1).toInt()]
        val loud = sorted[(sorted.lastIndex * 0.9).toInt()]
        return 20.0 * log10((loud + 1.0) / (quiet + 1.0))
    }

    private fun log10(value: Double): Double = ln(value) / ln(10.0)

    private data class WavInfo(
        val audioFormat: Int,
        val channels: Int,
        val sampleRate: Int,
        val bitsPerSample: Int,
        val dataOffset: Int,
        val dataSizeBytes: Int,
    )
}

private fun ByteArray.ascii(offset: Int, length: Int): String =
    copyOfRange(offset, offset + length).decodeToString()

private fun ByteArray.littleEndianInt(offset: Int): Int =
    (this[offset].toInt() and 0xff) or
        ((this[offset + 1].toInt() and 0xff) shl 8) or
        ((this[offset + 2].toInt() and 0xff) shl 16) or
        ((this[offset + 3].toInt() and 0xff) shl 24)

private fun ByteArray.littleEndianUnsignedShort(offset: Int): Int =
    (this[offset].toInt() and 0xff) or
        ((this[offset + 1].toInt() and 0xff) shl 8)

private fun ByteArray.littleEndianShort(offset: Int): Int =
    littleEndianUnsignedShort(offset).toShort().toInt()
