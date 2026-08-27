package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayTouchableRegionGeometryTest {
    @Test
    fun `right aligned idle bubble keeps only compact visible bounds touchable`() {
        val bounds = OverlayTouchableRegionGeometry.clippedBounds(
            windowWidth = 222,
            windowHeight = 75,
            candidates = listOf(OverlayTouchableBounds(160, 13, 208, 61)),
        )

        assertEquals(listOf(OverlayTouchableBounds(160, 13, 208, 61)), bounds)
        assertEquals(48, bounds.single().width)
        assertTrue(bounds.single().width < 222)
    }

    @Test
    fun `left aligned idle bubble keeps only compact visible bounds touchable`() {
        val bounds = OverlayTouchableRegionGeometry.clippedBounds(
            windowWidth = 222,
            windowHeight = 75,
            candidates = listOf(OverlayTouchableBounds(13, 13, 61, 61)),
        )

        assertEquals(listOf(OverlayTouchableBounds(13, 13, 61, 61)), bounds)
        assertEquals(48, bounds.single().width)
    }

    @Test
    fun `active row excludes stable frame padding`() {
        val bounds = OverlayTouchableRegionGeometry.clippedBounds(
            windowWidth = 222,
            windowHeight = 75,
            candidates = listOf(OverlayTouchableBounds(12, 13, 210, 61)),
        )

        assertEquals(listOf(OverlayTouchableBounds(12, 13, 210, 61)), bounds)
        assertEquals(198, bounds.single().width)
        assertEquals(48, bounds.single().height)
    }

    @Test
    fun `candidate bounds are clipped to the overlay window`() {
        val bounds = OverlayTouchableRegionGeometry.clippedBounds(
            windowWidth = 222,
            windowHeight = 75,
            candidates = listOf(OverlayTouchableBounds(-5, -10, 230, 80)),
        )

        assertEquals(listOf(OverlayTouchableBounds(0, 0, 222, 75)), bounds)
    }

    @Test
    fun `empty or collapsed candidates produce no touchable bounds`() {
        val bounds = OverlayTouchableRegionGeometry.clippedBounds(
            windowWidth = 222,
            windowHeight = 75,
            candidates = listOf(
                OverlayTouchableBounds(10, 10, 10, 30),
                OverlayTouchableBounds(20, 40, 30, 40),
                OverlayTouchableBounds(230, 10, 240, 20),
            ),
        )

        assertTrue(bounds.isEmpty())
        assertTrue(
            OverlayTouchableRegionGeometry.clippedBounds(
                windowWidth = 222,
                windowHeight = 75,
                candidates = emptyList(),
            ).isEmpty(),
        )
    }
}
