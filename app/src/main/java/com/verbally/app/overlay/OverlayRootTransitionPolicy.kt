package com.verbally.app.overlay

enum class OverlayRootUpdateMode {
    REBUILD_WINDOW,
    REFRESH_IN_PLACE,
}

object OverlayRootTransitionPolicy {
    fun mode(previous: OverlayUiState, next: OverlayUiState): OverlayRootUpdateMode =
        if (previous.isReadyLayout() != next.isReadyLayout()) {
            OverlayRootUpdateMode.REBUILD_WINDOW
        } else {
            OverlayRootUpdateMode.REFRESH_IN_PLACE
        }

    private fun OverlayUiState.isReadyLayout(): Boolean =
        this == OverlayUiState.READY || this == OverlayUiState.REPAIR
}
