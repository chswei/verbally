package com.verbally.app.overlay

data class OverlayTouchableBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val width: Int
        get() = (right - left).coerceAtLeast(0)

    val height: Int
        get() = (bottom - top).coerceAtLeast(0)
}

object OverlayTouchableRegionGeometry {
    fun clippedBounds(
        windowWidth: Int,
        windowHeight: Int,
        candidates: List<OverlayTouchableBounds>,
    ): List<OverlayTouchableBounds> {
        if (windowWidth <= 0 || windowHeight <= 0) return emptyList()

        return candidates.mapNotNull { candidate ->
            val clipped = OverlayTouchableBounds(
                left = candidate.left.coerceIn(0, windowWidth),
                top = candidate.top.coerceIn(0, windowHeight),
                right = candidate.right.coerceIn(0, windowWidth),
                bottom = candidate.bottom.coerceIn(0, windowHeight),
            )
            clipped.takeIf { it.width > 0 && it.height > 0 }
        }
    }
}
