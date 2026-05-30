package com.verbally.app.overlay

import android.view.Gravity
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayPositionMemoryTest {
    @Test
    fun `uses default right center position before user moves bubble`() {
        val memory = OverlayPositionMemory()

        val position = memory.currentPosition()

        assertEquals(Gravity.END or Gravity.CENTER_VERTICAL, position.gravity)
        assertEquals(20, position.x)
        assertEquals(0, position.y)
    }

    @Test
    fun `resolves default right margin with runtime edge pixels before user moves bubble`() {
        val memory = OverlayPositionMemory()

        val position = memory.currentPosition(screenWidth = 1080, bubbleWidth = 80, edgeMargin = 60)

        assertEquals(Gravity.END or Gravity.CENTER_VERTICAL, position.gravity)
        assertEquals(60, position.x)
        assertEquals(0, position.y)
    }

    @Test
    fun `snaps moved bubble to left edge when released closer to left side`() {
        val memory = OverlayPositionMemory()

        val position = memory.rememberSnappedPosition(
            releasedX = 120,
            releasedY = 240,
            bubbleWidth = 80,
            screenWidth = 1080,
            edgeMargin = 24,
        )

        assertEquals(OverlayEdge.LEFT, position.edge)
        assertEquals(Gravity.TOP or Gravity.START, position.gravity)
        assertEquals(24, position.x)
        assertEquals(240, position.y)
        assertEquals(position, memory.currentPosition(screenWidth = 1080, bubbleWidth = 80, edgeMargin = 24))
    }

    @Test
    fun `snaps moved bubble to right edge with end gravity`() {
        val memory = OverlayPositionMemory()

        val position = memory.rememberSnappedPosition(
            releasedX = 760,
            releasedY = 96,
            bubbleWidth = 80,
            screenWidth = 1080,
            edgeMargin = 24,
        )

        assertEquals(OverlayEdge.RIGHT, position.edge)
        assertEquals(Gravity.TOP or Gravity.END, position.gravity)
        assertEquals(24, position.x)
        assertEquals(96, position.y)
        assertEquals(position, memory.currentPosition(screenWidth = 1080, bubbleWidth = 80, edgeMargin = 24))
    }

    @Test
    fun `keeps saved right edge position anchored with end gravity`() {
        val memory = OverlayPositionMemory(
            initialPosition = OverlayPosition(
                edge = OverlayEdge.RIGHT,
                gravity = Gravity.TOP or Gravity.END,
                x = 20,
                y = 96,
            ),
        )

        val position = memory.currentPosition(screenWidth = 800, bubbleWidth = 72, edgeMargin = 20)

        assertEquals(OverlayEdge.RIGHT, position.edge)
        assertEquals(Gravity.TOP or Gravity.END, position.gravity)
        assertEquals(20, position.x)
        assertEquals(96, position.y)
    }

    @Test
    fun `recomputes saved position inside current screen bounds`() {
        val memory = OverlayPositionMemory(
            initialPosition = OverlayPosition(
                edge = OverlayEdge.RIGHT,
                gravity = Gravity.TOP or Gravity.START,
                x = 2_224,
                y = 820,
            ),
        )

        val position = memory.currentPosition(
            screenWidth = 1080,
            screenHeight = 600,
            bubbleWidth = 80,
            bubbleHeight = 80,
            edgeMargin = 24,
        )

        assertEquals(OverlayEdge.RIGHT, position.edge)
        assertEquals(Gravity.TOP or Gravity.END, position.gravity)
        assertEquals(24, position.x)
        assertEquals(496, position.y)
    }
}
