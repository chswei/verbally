package com.verbally.app.overlay

import android.view.View
import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverlayTouchableRegionCoordinatesTest {
    @Test
    fun nestedBubbleBoundsAreTranslatedIntoStableRootCoordinates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = FrameLayout(context)
        val motionFrame = FrameLayout(context)
        val bubble = View(context)
        root.addView(motionFrame)
        motionFrame.addView(bubble)

        root.layout(0, 0, 222, 75)
        motionFrame.layout(147, 0, 222, 75)
        bubble.layout(13, 13, 61, 61)

        assertEquals(
            OverlayTouchableBounds(160, 13, 208, 61),
            OverlayTouchableRegionCoordinates.boundsInRoot(root, bubble),
        )
    }

    @Test
    fun detachedSurfaceDoesNotProduceRootLocalBounds() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val root = FrameLayout(context).apply { layout(0, 0, 222, 75) }
        val detachedBubble = View(context).apply { layout(0, 0, 48, 48) }

        assertNull(OverlayTouchableRegionCoordinates.boundsInRoot(root, detachedBubble))
    }
}
