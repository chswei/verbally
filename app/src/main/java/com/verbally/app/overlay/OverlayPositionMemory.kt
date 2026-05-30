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
        return resolvedPosition(
            screenHeight = null,
            bubbleHeight = null,
            edgeMargin = edgeMargin,
        )
    }

    fun currentPosition(
        screenWidth: Int,
        screenHeight: Int,
        bubbleWidth: Int,
        bubbleHeight: Int,
        edgeMargin: Int,
    ): OverlayPosition {
        return resolvedPosition(
            screenHeight = screenHeight,
            bubbleHeight = bubbleHeight,
            edgeMargin = edgeMargin,
        )
    }

    private fun resolvedPosition(
        screenHeight: Int?,
        bubbleHeight: Int?,
        edgeMargin: Int,
    ): OverlayPosition {
        val position = currentPosition()
        val boundedY = yWithinBounds(
            y = position.y,
            screenHeight = screenHeight,
            bubbleHeight = bubbleHeight,
            edgeMargin = edgeMargin,
        )
        val edge = position.edge ?: return position.copy(
            x = edgeMargin.coerceAtLeast(0),
            y = boundedY,
        )
        return position.copy(
            gravity = gravityForEdge(edge),
            x = edgeMargin.coerceAtLeast(0),
            y = boundedY,
        )
    }

    fun rememberSnappedPosition(
        releasedX: Int,
        releasedY: Int,
        bubbleWidth: Int,
        screenWidth: Int,
        edgeMargin: Int,
        screenHeight: Int? = null,
        bubbleHeight: Int? = null,
    ): OverlayPosition {
        val bubbleCenterX = releasedX + bubbleWidth / 2
        val edge = if (bubbleCenterX < screenWidth / 2) OverlayEdge.LEFT else OverlayEdge.RIGHT
        val position = OverlayPosition(
            edge = edge,
            gravity = gravityForEdge(edge),
            x = edgeMargin.coerceAtLeast(0),
            y = yWithinBounds(
                y = releasedY,
                screenHeight = screenHeight,
                bubbleHeight = bubbleHeight,
                edgeMargin = edgeMargin,
            ),
        )
        movedPosition = position
        return position
    }

    private fun gravityForEdge(edge: OverlayEdge): Int =
        when (edge) {
            OverlayEdge.LEFT -> Gravity.TOP or Gravity.START
            OverlayEdge.RIGHT -> Gravity.TOP or Gravity.END
        }

    private fun yWithinBounds(
        y: Int,
        screenHeight: Int?,
        bubbleHeight: Int?,
        edgeMargin: Int,
    ): Int {
        val safeScreenHeight = screenHeight?.coerceAtLeast(0) ?: return y.coerceAtLeast(0)
        val safeBubbleHeight = bubbleHeight?.coerceAtLeast(0) ?: return y.coerceAtLeast(0)
        val safeMargin = edgeMargin.coerceAtLeast(0)
        val maxY = (safeScreenHeight - safeBubbleHeight - safeMargin).coerceAtLeast(0)
        return y.coerceIn(0, maxY)
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
