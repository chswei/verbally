package com.verbally.app.overlay

import android.view.Gravity

enum class OverlayEdge {
    LEFT,
    RIGHT,
}

data class OverlayPosition(
    val edge: OverlayEdge?,
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

    fun currentPosition(screenWidth: Int, bubbleWidth: Int, edgeMargin: Int): OverlayPosition {
        val position = currentPosition()
        val edge = position.edge ?: return position.copy(
            x = edgeMargin.coerceAtLeast(0),
        )
        return position.copy(
            gravity = Gravity.TOP or Gravity.START,
            x = xForEdge(edge, screenWidth, bubbleWidth, edgeMargin),
        )
    }

    fun rememberSnappedPosition(
        releasedX: Int,
        releasedY: Int,
        bubbleWidth: Int,
        screenWidth: Int,
        edgeMargin: Int,
    ): OverlayPosition {
        val bubbleCenterX = releasedX + bubbleWidth / 2
        val edge = if (bubbleCenterX < screenWidth / 2) OverlayEdge.LEFT else OverlayEdge.RIGHT
        val position = OverlayPosition(
            edge = edge,
            gravity = Gravity.TOP or Gravity.START,
            x = xForEdge(edge, screenWidth, bubbleWidth, edgeMargin),
            y = releasedY.coerceAtLeast(0),
        )
        movedPosition = position
        return position
    }

    private fun xForEdge(edge: OverlayEdge, screenWidth: Int, bubbleWidth: Int, edgeMargin: Int): Int {
        val safeScreenWidth = screenWidth.coerceAtLeast(0)
        val safeBubbleWidth = bubbleWidth.coerceAtLeast(0)
        val safeMargin = edgeMargin.coerceAtLeast(0)
        return when (edge) {
            OverlayEdge.LEFT -> safeMargin
            OverlayEdge.RIGHT -> (safeScreenWidth - safeBubbleWidth - safeMargin).coerceAtLeast(safeMargin)
        }
    }

    companion object {
        val DEFAULT_POSITION = OverlayPosition(
            edge = null,
            gravity = Gravity.END or Gravity.CENTER_VERTICAL,
            x = OverlayVisualDefaults.EDGE_MARGIN_DP,
            y = 0,
        )
    }
}
