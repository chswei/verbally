package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayVisualDefaultsTest {
    @Test
    fun `uses reference-like compact rounded-square metrics`() {
        assertEquals(20, OverlayVisualDefaults.EDGE_MARGIN_DP)
        assertEquals(50, OverlayVisualDefaults.BUBBLE_SIZE_DP)
        assertEquals(16, OverlayVisualDefaults.BUBBLE_CORNER_RADIUS_DP)
        assertEquals(25, OverlayVisualDefaults.ICON_SIZE_DP)
        assertEquals(50, OverlayVisualDefaults.ACTIVE_BUTTON_SIZE_DP)
        assertEquals(98, OverlayVisualDefaults.ACTIVE_CAPSULE_WIDTH_DP)
        assertEquals(50, OverlayVisualDefaults.ACTIVE_CAPSULE_HEIGHT_DP)
        assertEquals(5, OverlayVisualDefaults.ACTIVE_SPACING_DP)
    }
}
