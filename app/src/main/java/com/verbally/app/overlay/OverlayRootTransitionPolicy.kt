package com.verbally.app.overlay

enum class OverlayRootUpdateMode {
    REFRESH_IN_PLACE,
}

object OverlayRootTransitionPolicy {
    fun mode(previous: OverlayUiState, next: OverlayUiState): OverlayRootUpdateMode =
        OverlayRootUpdateMode.REFRESH_IN_PLACE
}
