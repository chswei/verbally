package com.verbally.app.overlay

enum class OverlayRootLifecycleAction {
    ATTACH_ROOT,
    SHOW_ATTACHED_ROOT,
    DETACH_ROOT,
    KEEP_DETACHED,
}

object OverlayRootLifecyclePolicy {
    fun actionForShow(
        overlayPermissionGranted: Boolean,
        rootAttached: Boolean,
    ): OverlayRootLifecycleAction =
        if (!overlayPermissionGranted) {
            if (rootAttached) {
                OverlayRootLifecycleAction.DETACH_ROOT
            } else {
                OverlayRootLifecycleAction.KEEP_DETACHED
            }
        } else if (rootAttached) {
            OverlayRootLifecycleAction.SHOW_ATTACHED_ROOT
        } else {
            OverlayRootLifecycleAction.ATTACH_ROOT
        }

    fun actionForHide(rootAttached: Boolean): OverlayRootLifecycleAction =
        if (rootAttached) {
            OverlayRootLifecycleAction.DETACH_ROOT
        } else {
            OverlayRootLifecycleAction.KEEP_DETACHED
        }
}
