package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayWindowTouchPolicyTest {
    @Test
    fun `pass through alpha uses Android maximum obscuring opacity`() {
        assertEquals(
            0.8f,
            OverlayWindowTouchPolicy.passThroughWindowAlpha(0.8f),
            0f,
        )
        assertEquals(
            0.65f,
            OverlayWindowTouchPolicy.passThroughWindowAlpha(0.65f),
            0f,
        )
    }

    @Test
    fun `pass through alpha stays within valid window alpha range`() {
        assertEquals(
            1f,
            OverlayWindowTouchPolicy.passThroughWindowAlpha(1.2f),
            0f,
        )
        assertEquals(
            0f,
            OverlayWindowTouchPolicy.passThroughWindowAlpha(-0.2f),
            0f,
        )
    }
}
