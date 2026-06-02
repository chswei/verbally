package com.verbally.app.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import kotlin.concurrent.thread
import kotlin.math.abs

interface AudioRecorder {
    fun start(): File
    fun stop(): File?
    fun stopAndDelete()
    fun currentAmplitude(): Int
    fun delete(file: File?)
}

internal object TemporaryAudioRecorderDefaults {
    const val AUDIO_SOURCE: Int = MediaRecorder.AudioSource.MIC
    const val SAMPLE_RATE_HZ: Int = 16_000
    const val CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_MONO
    const val CHANNEL_COUNT: Int = 1
    const val AUDIO_ENCODING: Int = AudioFormat.ENCODING_PCM_16BIT
    const val BITS_PER_SAMPLE: Int = 16
    const val FILE_EXTENSION: String = ".wav"

    internal const val BYTES_PER_SAMPLE: Int = BITS_PER_SAMPLE / 8
    internal const val MIN_BUFFER_DURATION_MILLIS: Int = 100
}

class TemporaryAudioRecorder(
    private val context: Context,
    private val maxDurationMillis: Int = 5 * 60 * 1000,
    private val audioSource: Int = TemporaryAudioRecorderDefaults.AUDIO_SOURCE,
) : AudioRecorder {
    @Volatile
    private var recording = false

    @Volatile
    private var latestAmplitude = 0

    private var audioRecord: AudioRecord? = null
    private var currentFile: File? = null
    private var recordingThread: Thread? = null

    override fun start(): File {
        stopAndDelete()
        val bufferSizeInBytes = bufferSizeInBytes()
        val recorder = createAudioRecord(bufferSizeInBytes)
        var output: File? = null

        return try {
            val recordingFile = File.createTempFile(
                "verbally-",
                TemporaryAudioRecorderDefaults.FILE_EXTENSION,
                context.cacheDir,
            )
            output = recordingFile
            val writerThread = thread(start = false, name = "VerballyAudioRecordWriter") {
                writeAudioDataToFile(recordingFile, recorder, bufferSizeInBytes)
            }
            latestAmplitude = 0
            currentFile = recordingFile
            audioRecord = recorder
            recordingThread = writerThread
            recording = true
            recorder.startRecording()
            writerThread.start()
            recordingFile
        } catch (error: Throwable) {
            recording = false
            currentFile = null
            audioRecord = null
            recordingThread = null
            runCatching { recorder.release() }
            output?.delete()
            throw error
        }
    }

    override fun stop(): File? {
        val file = currentFile
        val recorder = audioRecord
        val writerThread = recordingThread
        recording = false
        currentFile = null
        audioRecord = null
        recordingThread = null
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        if (writerThread != null && writerThread != Thread.currentThread()) {
            runCatching { writerThread.join() }
        }
        return file
    }

    override fun stopAndDelete() {
        val file = stop()
        file?.delete()
    }

    override fun currentAmplitude(): Int =
        latestAmplitude

    override fun delete(file: File?) {
        file?.delete()
    }

    private fun createAudioRecord(bufferSizeInBytes: Int): AudioRecord {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("RECORD_AUDIO permission is required to start dictation recording.")
        }
        return AudioRecord(
            audioSource,
            TemporaryAudioRecorderDefaults.SAMPLE_RATE_HZ,
            TemporaryAudioRecorderDefaults.CHANNEL_CONFIG,
            TemporaryAudioRecorderDefaults.AUDIO_ENCODING,
            bufferSizeInBytes,
        )
    }

    private fun bufferSizeInBytes(): Int {
        val minimum = AudioRecord.getMinBufferSize(
            TemporaryAudioRecorderDefaults.SAMPLE_RATE_HZ,
            TemporaryAudioRecorderDefaults.CHANNEL_CONFIG,
            TemporaryAudioRecorderDefaults.AUDIO_ENCODING,
        )
        val fallback = TemporaryAudioRecorderDefaults.SAMPLE_RATE_HZ *
            TemporaryAudioRecorderDefaults.CHANNEL_COUNT *
            TemporaryAudioRecorderDefaults.BYTES_PER_SAMPLE *
            TemporaryAudioRecorderDefaults.MIN_BUFFER_DURATION_MILLIS / 1_000
        return minimum.coerceAtLeast(fallback)
    }

    private fun writeAudioDataToFile(
        output: File,
        recorder: AudioRecord,
        bufferSizeInBytes: Int,
    ) {
        val buffer = ByteArray(bufferSizeInBytes)
        var dataSizeBytes = 0L
        val startedAtMillis = System.currentTimeMillis()
        try {
            FileOutputStream(output).use { stream ->
                WavFileWriter.writeHeader(stream, dataSizeBytes = 0)
                while (recording && !maxDurationReached(startedAtMillis)) {
                    when (val bytesRead = recorder.read(buffer, 0, buffer.size)) {
                        in 1..Int.MAX_VALUE -> {
                            stream.write(buffer, 0, bytesRead)
                            dataSizeBytes += bytesRead.toLong()
                            latestAmplitude = PcmAmplitude.maxAmplitude(buffer, bytesRead)
                        }
                        AudioRecord.ERROR_INVALID_OPERATION,
                        AudioRecord.ERROR_BAD_VALUE,
                        AudioRecord.ERROR_DEAD_OBJECT,
                        -> {
                            recording = false
                        }
                    }
                }
            }
        } finally {
            if (maxDurationReached(startedAtMillis)) {
                recording = false
                runCatching { recorder.stop() }
            }
            runCatching { WavFileWriter.patchHeader(output, dataSizeBytes) }
        }
    }

    private fun maxDurationReached(startedAtMillis: Long): Boolean =
        maxDurationMillis > 0 &&
            System.currentTimeMillis() - startedAtMillis >= maxDurationMillis
}

internal object PcmAmplitude {
    fun maxAmplitude(buffer: ByteArray, bytesRead: Int): Int {
        val limit = bytesRead.coerceAtLeast(0).coerceAtMost(buffer.size)
        var maxAmplitude = 0
        var index = 0
        while (index + 1 < limit) {
            val low = buffer[index].toInt() and 0xff
            val high = buffer[index + 1].toInt()
            val sample = ((high shl 8) or low).toShort().toInt()
            val amplitude = if (sample == Short.MIN_VALUE.toInt()) {
                Short.MAX_VALUE.toInt()
            } else {
                abs(sample)
            }
            if (amplitude > maxAmplitude) maxAmplitude = amplitude
            index += TemporaryAudioRecorderDefaults.BYTES_PER_SAMPLE
        }
        return maxAmplitude
    }
}

internal object WavFileWriter {
    private const val PCM_FORMAT: Int = 1
    private const val HEADER_SIZE_BYTES: Int = 44
    private const val RIFF_SIZE_MINUS_8: Int = HEADER_SIZE_BYTES - 8
    private const val FORMAT_CHUNK_SIZE: Int = 16
    private const val DATA_SIZE_OFFSET: Long = 40
    private const val RIFF_SIZE_OFFSET: Long = 4

    fun writeHeader(output: OutputStream, dataSizeBytes: Long) {
        val dataSize = dataSizeBytes.toChunkSize()
        val byteRate = TemporaryAudioRecorderDefaults.SAMPLE_RATE_HZ *
            TemporaryAudioRecorderDefaults.CHANNEL_COUNT *
            TemporaryAudioRecorderDefaults.BYTES_PER_SAMPLE
        val blockAlign = TemporaryAudioRecorderDefaults.CHANNEL_COUNT *
            TemporaryAudioRecorderDefaults.BYTES_PER_SAMPLE

        output.writeAscii("RIFF")
        output.writeLittleEndianInt(RIFF_SIZE_MINUS_8 + dataSize)
        output.writeAscii("WAVE")
        output.writeAscii("fmt ")
        output.writeLittleEndianInt(FORMAT_CHUNK_SIZE)
        output.writeLittleEndianShort(PCM_FORMAT)
        output.writeLittleEndianShort(TemporaryAudioRecorderDefaults.CHANNEL_COUNT)
        output.writeLittleEndianInt(TemporaryAudioRecorderDefaults.SAMPLE_RATE_HZ)
        output.writeLittleEndianInt(byteRate)
        output.writeLittleEndianShort(blockAlign)
        output.writeLittleEndianShort(TemporaryAudioRecorderDefaults.BITS_PER_SAMPLE)
        output.writeAscii("data")
        output.writeLittleEndianInt(dataSize)
    }

    fun patchHeader(file: File, dataSizeBytes: Long) {
        val dataSize = dataSizeBytes.toChunkSize()
        RandomAccessFile(file, "rw").use { wav ->
            wav.seek(RIFF_SIZE_OFFSET)
            wav.writeLittleEndianInt(RIFF_SIZE_MINUS_8 + dataSize)
            wav.seek(DATA_SIZE_OFFSET)
            wav.writeLittleEndianInt(dataSize)
        }
    }

    private fun Long.toChunkSize(): Int =
        coerceIn(0, UInt.MAX_VALUE.toLong()).toInt()

    private fun OutputStream.writeAscii(value: String) {
        value.forEach { write(it.code) }
    }

    private fun OutputStream.writeLittleEndianInt(value: Int) {
        write(value and 0xff)
        write((value shr 8) and 0xff)
        write((value shr 16) and 0xff)
        write((value shr 24) and 0xff)
    }

    private fun OutputStream.writeLittleEndianShort(value: Int) {
        write(value and 0xff)
        write((value shr 8) and 0xff)
    }

    private fun RandomAccessFile.writeLittleEndianInt(value: Int) {
        write(value and 0xff)
        write((value shr 8) and 0xff)
        write((value shr 16) and 0xff)
        write((value shr 24) and 0xff)
    }
}
