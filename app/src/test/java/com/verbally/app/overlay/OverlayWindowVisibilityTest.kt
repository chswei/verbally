package com.verbally.app.overlay

import android.view.WindowManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayWindowVisibilityTest {
    @Test
    fun `visible overlay is opaque and touchable`() {
        val flags = OverlayWindowVisibility.flagsFor(
            baseFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            visible = true,
        )

        assertEquals(1f, OverlayWindowVisibility.alphaFor(visible = true), 0f)
        assertTrue(OverlayWindowVisibility.isTouchable(flags))
    }

    @Test
    fun `hidden overlay is transparent and cannot intercept touches`() {
        val flags = OverlayWindowVisibility.flagsFor(
            baseFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            visible = false,
        )

        assertEquals(0f, OverlayWindowVisibility.alphaFor(visible = false), 0f)
        assertFalse(OverlayWindowVisibility.isTouchable(flags))
    }
}
