package com.verbally.app.overlay

import android.view.Gravity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayContentSwapPolicyTest {
    @Test
    fun `right anchored size changes defer content until after layout`() {
        assertTrue(
            OverlayContentSwapPolicy.deferUntilAfterLayout(
                currentGravity = Gravity.TOP or Gravity.END,
                currentWidth = 239,
                currentHeight = 239,
                nextWidth = 708,
                nextHeight = 239,
            ),
        )
    }

    @Test
    fun `left anchored size changes keep immediate content swap`() {
        assertFalse(
            OverlayContentSwapPolicy.deferUntilAfterLayout(
                currentGravity = Gravity.TOP or Gravity.START,
                currentWidth = 239,
                currentHeight = 239,
                nextWidth = 708,
                nextHeight = 239,
            ),
        )
    }

    @Test
    fun `right anchored same size updates do not defer`() {
        assertFalse(
            OverlayContentSwapPolicy.deferUntilAfterLayout(
                currentGravity = Gravity.TOP or Gravity.END,
                currentWidth = 708,
                currentHeight = 239,
                nextWidth = 708,
                nextHeight = 239,
            ),
        )
    }

    @Test
    fun `right anchored ready to recording animates active intro from bubble anchor`() {
        assertTrue(
            OverlayContentSwapPolicy.animateAnchoredIntro(
                previousState = OverlayUiState.READY,
                nextState = OverlayUiState.RECORDING,
                currentGravity = Gravity.TOP or Gravity.END,
            ),
        )
    }

    @Test
    fun `left anchored ready to recording also animates active intro from bubble anchor`() {
        assertTrue(
            OverlayContentSwapPolicy.animateAnchoredIntro(
                previousState = OverlayUiState.READY,
                nextState = OverlayUiState.RECORDING,
                currentGravity = Gravity.TOP or Gravity.START,
            ),
        )
    }

    @Test
    fun `right anchored ready state uses stable active frame`() {
        assertTrue(
            OverlayContentSwapPolicy.useStableEdgeFrame(
                state = OverlayUiState.READY,
                currentGravity = Gravity.TOP or Gravity.END,
            ),
        )
    }

    @Test
    fun `default end centered ready state uses stable active frame`() {
        assertTrue(
            OverlayContentSwapPolicy.useStableEdgeFrame(
                state = OverlayUiState.READY,
                currentGravity = Gravity.END or Gravity.CENTER_VERTICAL,
            ),
        )
    }

    @Test
    fun `left anchored ready state uses stable active frame`() {
        assertTrue(
            OverlayContentSwapPolicy.useStableEdgeFrame(
                state = OverlayUiState.READY,
                currentGravity = Gravity.TOP or Gravity.START,
            ),
        )
    }

    @Test
    fun `repair state keeps compact frame`() {
        assertFalse(
            OverlayContentSwapPolicy.useStableEdgeFrame(
                state = OverlayUiState.REPAIR,
                currentGravity = Gravity.TOP or Gravity.START,
            ),
        )
    }

    @Test
    fun `left anchored content aligns to start of stable frame`() {
        assertTrue(
            OverlayContentSwapPolicy.alignContentToStart(
                currentGravity = Gravity.TOP or Gravity.START,
            ),
        )
    }

    @Test
    fun `right anchored content aligns to end of stable frame`() {
        assertFalse(
            OverlayContentSwapPolicy.alignContentToStart(
                currentGravity = Gravity.TOP or Gravity.END,
            ),
        )
    }
}
