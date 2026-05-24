package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayHapticDefaultsTest {
    @Test
    fun `uses explicit overlay vibration defaults`() {
        assertEquals(12L, OverlayHapticDefaults.TAP_DURATION_MS)
        assertEquals(12L, OverlayHapticDefaults.DRAG_START_DURATION_MS)
        assertEquals(18L, OverlayHapticDefaults.SNAP_DURATION_MS)
    }
}
