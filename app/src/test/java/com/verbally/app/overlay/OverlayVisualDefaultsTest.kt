package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayVisualDefaultsTest {
    @Test
    fun `uses reference-like rounded-square idle metrics with Verbally proportions`() {
        assertEquals(7, OverlayVisualDefaults.EDGE_MARGIN_DP)
        assertEquals(75, OverlayVisualDefaults.MOTION_FRAME_SIZE_DP)
        assertEquals(48, OverlayVisualDefaults.BUBBLE_SIZE_DP)
        assertEquals(16, OverlayVisualDefaults.BUBBLE_CORNER_RADIUS_DP)
        assertEquals(22, OverlayVisualDefaults.ICON_SIZE_DP)
    }

    @Test
    fun `uses compact three-part active control metrics`() {
        assertEquals(48, OverlayVisualDefaults.ACTIVE_BUTTON_SIZE_DP)
        assertEquals(22, OverlayVisualDefaults.ACTIVE_BUTTON_ICON_SIZE_DP)
        assertEquals(90, OverlayVisualDefaults.ACTIVE_CAPSULE_WIDTH_DP)
        assertEquals(48, OverlayVisualDefaults.ACTIVE_CAPSULE_HEIGHT_DP)
        assertEquals(6, OverlayVisualDefaults.ACTIVE_SPACING_DP)
        assertEquals(24, OverlayVisualDefaults.ACTIVE_CAPSULE_CORNER_RADIUS_DP)
        assertEquals(12, OverlayVisualDefaults.ACTIVE_FRAME_HORIZONTAL_PADDING_DP)
    }

    @Test
    fun `active controls use measured right anchored motion frame`() {
        assertEquals(198, OverlayVisualDefaults.ACTIVE_ROW_WIDTH_DP)
        assertEquals(222, OverlayVisualDefaults.ACTIVE_FRAME_WIDTH_DP)
        assertEquals(0L, OverlayVisualDefaults.MOTION_ANIMATION_DURATION_MS)
        assertEquals(120L, OverlayVisualDefaults.ACTIVE_REVEAL_ANIMATION_DURATION_MS)
    }
}
