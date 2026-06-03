package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayRootLifecyclePolicyTest {
    @Test
    fun `show attaches root when overlay permission is granted and no root is attached`() {
        val action = OverlayRootLifecyclePolicy.actionForShow(
            overlayPermissionGranted = true,
            rootAttached = false,
        )

        assertEquals(OverlayRootLifecycleAction.ATTACH_ROOT, action)
    }

    @Test
    fun `show keeps attached root visible when overlay permission is granted`() {
        val action = OverlayRootLifecyclePolicy.actionForShow(
            overlayPermissionGranted = true,
            rootAttached = true,
        )

        assertEquals(OverlayRootLifecycleAction.SHOW_ATTACHED_ROOT, action)
    }

    @Test
    fun `show removes attached root when overlay permission is revoked`() {
        val action = OverlayRootLifecyclePolicy.actionForShow(
            overlayPermissionGranted = false,
            rootAttached = true,
        )

        assertEquals(OverlayRootLifecycleAction.DETACH_ROOT, action)
    }

    @Test
    fun `hide removes attached root instead of retaining transparent root`() {
        val action = OverlayRootLifecyclePolicy.actionForHide(rootAttached = true)

        assertEquals(OverlayRootLifecycleAction.DETACH_ROOT, action)
    }

    @Test
    fun `hide keeps detached state when no root is attached`() {
        val action = OverlayRootLifecyclePolicy.actionForHide(rootAttached = false)

        assertEquals(OverlayRootLifecycleAction.KEEP_DETACHED, action)
    }
}
