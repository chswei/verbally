package com.verbally.app.overlay

enum class OverlayUiState {
    READY,
    RECORDING,
    PROCESSING,
}

class OverlaySessionStateMachine(
    initialState: OverlayUiState = OverlayUiState.READY,
) {
    var state: OverlayUiState = initialState
        private set

    fun onReadyBubbleTapped() {
        if (state == OverlayUiState.READY) {
            state = OverlayUiState.RECORDING
        }
    }

    fun onConfirmTapped() {
        if (state == OverlayUiState.RECORDING) {
            state = OverlayUiState.PROCESSING
        }
    }

    fun onCancelTapped() {
        if (state == OverlayUiState.RECORDING) {
            state = OverlayUiState.READY
        }
    }

    fun onProcessingFinished() {
        if (state == OverlayUiState.PROCESSING) {
            state = OverlayUiState.READY
        }
    }

    fun forceState(next: OverlayUiState) {
        state = next
    }
}
