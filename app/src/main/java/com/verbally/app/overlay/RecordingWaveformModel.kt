package com.verbally.app.overlay

import kotlin.math.sin

internal object RecordingWaveformModel {
    const val HORIZONTAL_INSET_FRACTION = 0.20f

    private const val MIN_HEIGHT_SCALE = 0.06f
    private const val MAX_HEIGHT_SCALE = 0.72f
    private const val MAX_WIDTH_SCALE = 0.34f
    private val contour = floatArrayOf(
        0.18f,
        0.36f,
        0.62f,
        0.86f,
        1.00f,
        0.86f,
        0.62f,
        0.36f,
        0.18f,
    )

    data class Mark(
        val heightScale: Float,
        val widthScale: Float,
    )

    fun marks(level: Float, phase: Float): List<Mark> {
        val liveLevel = level.coerceIn(0f, 1f)
        val liveScale = 0.18f + liveLevel * 0.82f
        return contour.mapIndexed { index, base ->
            val wave = sin(phase * FULL_CYCLE + index * 0.55f)
            val animatedScale = 0.90f + 0.10f * ((wave + 1f) / 2f)
            Mark(
                heightScale = (MIN_HEIGHT_SCALE + base * 0.66f * liveScale * animatedScale)
                    .coerceIn(MIN_HEIGHT_SCALE, MAX_HEIGHT_SCALE),
                widthScale = MAX_WIDTH_SCALE,
            )
        }
    }

    private const val FULL_CYCLE = (Math.PI * 2).toFloat()
}
