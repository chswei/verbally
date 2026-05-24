package com.verbally.app.overlay

import com.verbally.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayColorDefaultsTest {
    @Test
    fun `uses the Verbally palette roles for ready and active bubble states`() {
        assertEquals(R.color.verbally_brand_blue, OverlayColorDefaults.READY_ICON_COLOR_RES)
        assertEquals(R.color.overlay_surface_frosted, OverlayColorDefaults.READY_BUBBLE_BACKGROUND_RES)
        assertEquals(R.color.overlay_surface_frosted, OverlayColorDefaults.ACTIVE_CANCEL_BACKGROUND_RES)
        assertEquals(R.color.verbally_brand_blue, OverlayColorDefaults.ACTIVE_CANCEL_ICON_COLOR_RES)
        assertEquals(R.color.overlay_surface_frosted, OverlayColorDefaults.ACTIVE_CENTER_BACKGROUND_RES)
        assertEquals(R.color.verbally_brand_blue, OverlayColorDefaults.ACTIVE_WAVEFORM_COLOR_RES)
        assertEquals(R.color.overlay_brand_blue_translucent, OverlayColorDefaults.ACTIVE_CONFIRM_BACKGROUND_RES)
        assertEquals(R.color.overlay_on_brand, OverlayColorDefaults.ACTIVE_CONFIRM_ICON_COLOR_RES)
        assertEquals(R.color.overlay_surface_frosted, OverlayColorDefaults.PROCESSING_BACKGROUND_RES)
        assertEquals(R.color.verbally_brand_blue, OverlayColorDefaults.PROCESSING_ACCENT_COLOR_RES)
        assertEquals(R.color.overlay_brand_blue_soft, OverlayColorDefaults.PROCESSING_TRACK_COLOR_RES)
    }
}
