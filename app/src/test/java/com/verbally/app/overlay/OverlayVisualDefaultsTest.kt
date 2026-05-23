package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayVisualDefaultsTest {
    @Test
    fun `uses reference-like compact rounded-square metrics`() {
        assertEquals(20, OverlayVisualDefaults.EDGE_MARGIN_DP)
        assertEquals(44, OverlayVisualDefaults.BUBBLE_SIZE_DP)
        assertEquals(15, OverlayVisualDefaults.BUBBLE_CORNER_RADIUS_DP)
        assertEquals(24, OverlayVisualDefaults.ICON_SIZE_DP)
        assertEquals(44, OverlayVisualDefaults.ACTIVE_BUTTON_SIZE_DP)
        assertEquals(156, OverlayVisualDefaults.ACTIVE_CAPSULE_WIDTH_DP)
        assertEquals(44, OverlayVisualDefaults.ACTIVE_CAPSULE_HEIGHT_DP)
    }
}
