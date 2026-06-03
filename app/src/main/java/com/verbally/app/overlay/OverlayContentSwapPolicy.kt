package com.verbally.app.overlay

import android.view.Gravity

object OverlayContentSwapPolicy {
    fun deferUntilAfterLayout(
        currentGravity: Int,
        currentWidth: Int,
        currentHeight: Int,
        nextWidth: Int,
        nextHeight: Int,
    ): Boolean =
        isRightAnchored(currentGravity) &&
            (currentWidth != nextWidth || currentHeight != nextHeight)

    fun animateAnchoredIntro(
        previousState: OverlayUiState?,
        nextState: OverlayUiState,
        currentGravity: Int,
    ): Boolean =
        previousState == OverlayUiState.READY &&
            nextState == OverlayUiState.RECORDING &&
            isEdgeAnchored(currentGravity)

    fun useStableEdgeFrame(
        state: OverlayUiState,
        currentGravity: Int,
    ): Boolean =
        state in STABLE_FRAME_STATES && isEdgeAnchored(currentGravity)

    fun alignContentToStart(currentGravity: Int): Boolean =
        isLeftAnchored(currentGravity) && !isRightAnchored(currentGravity)

    private fun isEdgeAnchored(gravity: Int): Boolean =
        isLeftAnchored(gravity) || isRightAnchored(gravity)

    private fun isLeftAnchored(gravity: Int): Boolean =
        gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK == Gravity.START ||
            gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.LEFT

    private fun isRightAnchored(gravity: Int): Boolean =
        gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK == Gravity.END ||
            gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.RIGHT

    private val STABLE_FRAME_STATES = setOf(
        OverlayUiState.READY,
        OverlayUiState.RECORDING,
        OverlayUiState.PROCESSING,
    )
}
