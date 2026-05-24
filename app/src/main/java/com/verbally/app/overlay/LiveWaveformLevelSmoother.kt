package com.verbally.app.overlay

import kotlin.math.sqrt

class LiveWaveformLevelSmoother(
    private val floor: Float = 0f,
    private val riseFactor: Float = 0.52f,
    private val decayFactor: Float = 0.14f,
) {
    private var currentLevel = floor

    fun update(maxAmplitude: Int): Float {
        val normalized = normalize(maxAmplitude)
        val factor = if (normalized >= currentLevel) riseFactor else decayFactor
        currentLevel += (normalized - currentLevel) * factor
        currentLevel = currentLevel.coerceIn(floor, 1f)
        return currentLevel
    }

    fun reset(): Float {
        currentLevel = floor
        return currentLevel
    }

    private fun normalize(maxAmplitude: Int): Float {
        val clamped = maxAmplitude.coerceAtLeast(0).coerceAtMost(SHORT_MAX_AMPLITUDE)
        if (clamped == 0) return floor
        val curved = sqrt(clamped / SHORT_MAX_AMPLITUDE.toFloat())
        return (floor + curved * (1f - floor)).coerceIn(floor, 1f)
    }

    companion object {
        private const val SHORT_MAX_AMPLITUDE = 32_767
    }
}
