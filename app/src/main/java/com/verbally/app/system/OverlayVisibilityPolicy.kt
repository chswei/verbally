package com.verbally.app.system

enum class OverlayVisibilityDecision {
    SHOW,
    HIDE,
    KEEP,
}

data class OverlayVisibilityEvent(
    val eventType: Int,
    val packageName: String?,
    val eventTime: Long,
    val sourceEditable: Boolean?,
    val sourceFocused: Boolean?,
    val focusedEditable: Boolean?,
    val inputMethodEvent: Boolean = false,
    val inputMethodVisible: Boolean = false,
)

class OverlayVisibilityPolicy {
    fun decide(
        event: OverlayVisibilityEvent,
        overlayShown: Boolean,
    ): OverlayVisibilityDecision =
        if (event.inputMethodVisible) {
            if (overlayShown) OverlayVisibilityDecision.KEEP else OverlayVisibilityDecision.SHOW
        } else {
            if (overlayShown) OverlayVisibilityDecision.HIDE else OverlayVisibilityDecision.KEEP
        }
}
