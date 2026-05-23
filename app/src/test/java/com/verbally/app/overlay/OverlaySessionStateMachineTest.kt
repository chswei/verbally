package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlaySessionStateMachineTest {
    @Test
    fun processingCompletionReturnsToReadyForNextRecording() {
        val machine = OverlaySessionStateMachine()

        assertEquals(OverlayUiState.READY, machine.state)

        machine.onReadyBubbleTapped()
        assertEquals(OverlayUiState.RECORDING, machine.state)

        machine.onConfirmTapped()
        assertEquals(OverlayUiState.PROCESSING, machine.state)

        machine.onProcessingFinished()
        assertEquals(OverlayUiState.READY, machine.state)

        machine.onReadyBubbleTapped()
        assertEquals(OverlayUiState.RECORDING, machine.state)
    }

    @Test
    fun cancelReturnsToReadyWithoutBreakingNextRecording() {
        val machine = OverlaySessionStateMachine()

        machine.onReadyBubbleTapped()
        assertEquals(OverlayUiState.RECORDING, machine.state)

        machine.onCancelTapped()
        assertEquals(OverlayUiState.READY, machine.state)

        machine.onReadyBubbleTapped()
        assertEquals(OverlayUiState.RECORDING, machine.state)
    }
}
