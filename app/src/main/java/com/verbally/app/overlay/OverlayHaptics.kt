package com.verbally.app.overlay

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class OverlayHaptics(context: Context) {
    private val vibrator: Vibrator? =
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator

    fun tap() {
        vibrate(OverlayHapticDefaults.TAP_DURATION_MS)
    }

    fun dragStart() {
        vibrate(OverlayHapticDefaults.DRAG_START_DURATION_MS)
    }

    fun snap() {
        vibrate(OverlayHapticDefaults.SNAP_DURATION_MS)
    }

    private fun vibrate(durationMs: Long) {
        val target = vibrator ?: return
        if (!target.hasVibrator()) return
        runCatching {
            target.vibrate(
                VibrationEffect.createOneShot(
                    durationMs,
                    VibrationEffect.DEFAULT_AMPLITUDE,
                ),
            )
        }
    }
}
