package com.verbally.app.overlay

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayRootTransitionPolicyTest {
    @Test
    fun `rebuilds window when ready bubble expands to recording controls`() {
        val mode = OverlayRootTransitionPolicy.mode(
            previous = OverlayUiState.READY,
            next = OverlayUiState.RECORDING,
        )

        assertEquals(OverlayRootUpdateMode.REBUILD_WINDOW, mode)
    }

    @Test
    fun `rebuilds window when active controls collapse back to ready bubble`() {
        val mode = OverlayRootTransitionPolicy.mode(
            previous = OverlayUiState.PROCESSING,
            next = OverlayUiState.READY,
        )

        assertEquals(OverlayRootUpdateMode.REBUILD_WINDOW, mode)
    }

    @Test
    fun `rebuilds window when recording controls cancel back to ready bubble`() {
        val mode = OverlayRootTransitionPolicy.mode(
            previous = OverlayUiState.RECORDING,
            next = OverlayUiState.READY,
        )

        assertEquals(OverlayRootUpdateMode.REBUILD_WINDOW, mode)
    }

    @Test
    fun `refreshes in place between active control states`() {
        val mode = OverlayRootTransitionPolicy.mode(
            previous = OverlayUiState.RECORDING,
            next = OverlayUiState.PROCESSING,
        )

        assertEquals(OverlayRootUpdateMode.REFRESH_IN_PLACE, mode)
    }

    @Test
    fun `refreshes in place between ready and repair bubble states`() {
        val mode = OverlayRootTransitionPolicy.mode(
            previous = OverlayUiState.READY,
            next = OverlayUiState.REPAIR,
        )

        assertEquals(OverlayRootUpdateMode.REFRESH_IN_PLACE, mode)
    }
}
