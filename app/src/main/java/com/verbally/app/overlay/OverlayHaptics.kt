package com.verbally.app.overlay

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class OverlayHaptics(context: Context) {
    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                target.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        VibrationEffect.DEFAULT_AMPLITUDE,
                    ),
                )
            } else {
                @Suppress("DEPRECATION")
                target.vibrate(durationMs)
            }
        }
    }
}
