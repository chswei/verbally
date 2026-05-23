package com.verbally.app.overlay

import android.view.Gravity

data class OverlayPosition(
    val gravity: Int,
    val x: Int,
    val y: Int,
)

class OverlayPositionMemory(
    initialPosition: OverlayPosition? = null,
) {
    private var movedPosition: OverlayPosition? = initialPosition

    fun currentPosition(): OverlayPosition =
        movedPosition ?: DEFAULT_POSITION

    fun rememberMovedPosition(x: Int, y: Int) {
        movedPosition = OverlayPosition(Gravity.TOP or Gravity.START, x, y)
    }

    companion object {
        val DEFAULT_POSITION = OverlayPosition(
            gravity = Gravity.END or Gravity.CENTER_VERTICAL,
            x = 24,
            y = 0,
        )
    }
}
