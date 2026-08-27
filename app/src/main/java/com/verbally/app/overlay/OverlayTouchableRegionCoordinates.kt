package com.verbally.app.overlay

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup

object OverlayTouchableRegionCoordinates {
    fun boundsInRoot(root: ViewGroup, target: View): OverlayTouchableBounds? {
        if (!target.isDescendantOf(root) || target.width <= 0 || target.height <= 0) return null

        val bounds = Rect(0, 0, target.width, target.height)
        root.offsetDescendantRectToMyCoords(target, bounds)
        return OverlayTouchableBounds(
            left = bounds.left,
            top = bounds.top,
            right = bounds.right,
            bottom = bounds.bottom,
        )
    }

    private fun View.isDescendantOf(ancestor: ViewGroup): Boolean {
        var currentParent = parent
        while (currentParent is View) {
            if (currentParent === ancestor) return true
            currentParent = currentParent.parent
        }
        return false
    }
}
