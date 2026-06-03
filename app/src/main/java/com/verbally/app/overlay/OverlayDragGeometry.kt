package com.verbally.app.overlay

object OverlayDragGeometry {
    fun visibleReleasedX(
        releasedWindowX: Int,
        windowWidth: Int,
        visibleWidth: Int,
        visibleOffset: Int = trailingVisibleOffset(windowWidth, visibleWidth),
    ): Int =
        releasedWindowX + visibleOffset.coerceIn(0, (windowWidth - visibleWidth).coerceAtLeast(0))

    fun boundedWindowXForVisibleControl(
        proposedWindowX: Int,
        windowWidth: Int,
        visibleWidth: Int,
        screenWidth: Int,
        visibleOffset: Int = trailingVisibleOffset(windowWidth, visibleWidth),
    ): Int {
        val boundedOffset = visibleOffset.coerceIn(0, (windowWidth - visibleWidth).coerceAtLeast(0))
        val minWindowX = -boundedOffset
        val maxWindowX = (screenWidth - visibleWidth - boundedOffset).coerceAtLeast(minWindowX)
        return proposedWindowX.coerceIn(minWindowX, maxWindowX)
    }

    private fun trailingVisibleOffset(windowWidth: Int, visibleWidth: Int): Int =
        (windowWidth - visibleWidth).coerceAtLeast(0)
}
