package com.verbally.app.overlay

import androidx.annotation.ColorRes
import com.verbally.app.R

object OverlayColorDefaults {
    @JvmField @ColorRes val READY_BUBBLE_BACKGROUND_RES = R.color.overlay_surface_frosted
    @JvmField @ColorRes val READY_ICON_COLOR_RES = R.color.verbally_brand_blue

    @JvmField @ColorRes val ACTIVE_CANCEL_BACKGROUND_RES = R.color.overlay_surface_frosted
    @JvmField @ColorRes val ACTIVE_CANCEL_ICON_COLOR_RES = R.color.verbally_brand_blue
    @JvmField @ColorRes val ACTIVE_CENTER_BACKGROUND_RES = R.color.overlay_surface_frosted
    @JvmField @ColorRes val ACTIVE_WAVEFORM_COLOR_RES = R.color.verbally_brand_blue
    @JvmField @ColorRes val ACTIVE_CONFIRM_BACKGROUND_RES = R.color.overlay_brand_blue_translucent
    @JvmField @ColorRes val ACTIVE_CONFIRM_ICON_COLOR_RES = R.color.overlay_on_brand

    @JvmField @ColorRes val PROCESSING_BACKGROUND_RES = R.color.overlay_surface_frosted
    @JvmField @ColorRes val PROCESSING_ACCENT_COLOR_RES = R.color.verbally_brand_blue
    @JvmField @ColorRes val PROCESSING_TRACK_COLOR_RES = R.color.overlay_brand_blue_soft
}
