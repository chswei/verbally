package com.verbally.app.overlay

import android.view.WindowManager

object OverlayWindowVisibility {
    const val VISIBLE_ALPHA = 1f
    const val HIDDEN_ALPHA = 0f

    fun alphaFor(visible: Boolean): Float =
        if (visible) VISIBLE_ALPHA else HIDDEN_ALPHA

    fun flagsFor(baseFlags: Int, visible: Boolean): Int =
        if (visible) {
            baseFlags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        } else {
            baseFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

    fun isTouchable(flags: Int): Boolean =
        flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE == 0
}
