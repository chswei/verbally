package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayVisualDefaultsTest {
    @Test
    fun `uses reference-like compact rounded-square metrics`() {
        assertEquals(20, OverlayVisualDefaults.EDGE_MARGIN_DP)
        assertEquals(48, OverlayVisualDefaults.BUBBLE_SIZE_DP)
        assertEquals(16, OverlayVisualDefaults.BUBBLE_CORNER_RADIUS_DP)
        assertEquals(28, OverlayVisualDefaults.ICON_SIZE_DP)
    }
}
