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
        assertEquals(24, position.x)
        assertEquals(0, position.y)
    }

    @Test
    fun `keeps user moved position until it is moved again`() {
        val memory = OverlayPositionMemory()

        memory.rememberMovedPosition(x = 120, y = 240)
        assertEquals(OverlayPosition(Gravity.TOP or Gravity.START, 120, 240), memory.currentPosition())

        memory.rememberMovedPosition(x = 48, y = 96)
        assertEquals(OverlayPosition(Gravity.TOP or Gravity.START, 48, 96), memory.currentPosition())
    }
}
