package com.verbally.app.overlay

object OverlayWindowTouchPolicy {
    fun passThroughWindowAlpha(maximumObscuringOpacityForTouch: Float): Float =
        maximumObscuringOpacityForTouch.coerceIn(0f, 1f)
}
