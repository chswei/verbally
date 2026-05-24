package com.verbally.app.audio

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class TemporaryAudioRecorder(
    private val context: Context,
    private val maxDurationMillis: Int = 5 * 60 * 1000,
) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun start(): File {
        stopAndDelete()
        val output = File.createTempFile("verbally-", ".m4a", context.cacheDir)
        val mediaRecorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setMaxDuration(maxDurationMillis)
            setOutputFile(output.absolutePath)
            prepare()
            start()
        }
        currentFile = output
        recorder = mediaRecorder
        return output
    }

    fun stop(): File? {
        val file = currentFile
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        currentFile = null
        return file
    }

    fun stopAndDelete() {
        val file = stop()
        file?.delete()
    }

    fun currentAmplitude(): Int =
        runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)

    fun delete(file: File?) {
        file?.delete()
    }
}
