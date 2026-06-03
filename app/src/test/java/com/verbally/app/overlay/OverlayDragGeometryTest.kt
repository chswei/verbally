package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayDragGeometryTest {
    @Test
    fun `uses trailing visible bubble position when stable frame is wider than bubble`() {
        val releasedX = OverlayDragGeometry.visibleReleasedX(
            releasedWindowX = 350,
            windowWidth = 708,
            visibleWidth = 239,
        )

        assertEquals(819, releasedX)
    }

    @Test
    fun `keeps released position unchanged when frame matches visible bubble`() {
        val releasedX = OverlayDragGeometry.visibleReleasedX(
            releasedWindowX = 22,
            windowWidth = 239,
            visibleWidth = 239,
        )

        assertEquals(22, releasedX)
    }

    @Test
    fun `allows wide stable frame to move negative so visible bubble reaches left edge`() {
        val boundedX = OverlayDragGeometry.boundedWindowXForVisibleControl(
            proposedWindowX = -900,
            windowWidth = 708,
            visibleWidth = 239,
            screenWidth = 1080,
            visibleOffset = 469,
        )

        assertEquals(-469, boundedX)
    }

    @Test
    fun `keeps wide stable frame inside right edge by visible bubble bounds`() {
        val boundedX = OverlayDragGeometry.boundedWindowXForVisibleControl(
            proposedWindowX = 900,
            windowWidth = 708,
            visibleWidth = 239,
            screenWidth = 1080,
            visibleOffset = 469,
        )

        assertEquals(372, boundedX)
    }

    @Test
    fun `left aligned stable frame clamps by visible bubble at frame start`() {
        val leftBound = OverlayDragGeometry.boundedWindowXForVisibleControl(
            proposedWindowX = -900,
            windowWidth = 708,
            visibleWidth = 239,
            screenWidth = 1080,
            visibleOffset = 0,
        )
        val rightBound = OverlayDragGeometry.boundedWindowXForVisibleControl(
            proposedWindowX = 900,
            windowWidth = 708,
            visibleWidth = 239,
            screenWidth = 1080,
            visibleOffset = 0,
        )

        assertEquals(0, leftBound)
        assertEquals(841, rightBound)
    }

    @Test
    fun `normal compact frame still clamps to screen bounds`() {
        val leftBound = OverlayDragGeometry.boundedWindowXForVisibleControl(
            proposedWindowX = -42,
            windowWidth = 239,
            visibleWidth = 239,
            screenWidth = 1080,
        )
        val rightBound = OverlayDragGeometry.boundedWindowXForVisibleControl(
            proposedWindowX = 900,
            windowWidth = 239,
            visibleWidth = 239,
            screenWidth = 1080,
        )

        assertEquals(0, leftBound)
        assertEquals(841, rightBound)
    }
}
