package com.verbally.app.system

import android.view.accessibility.AccessibilityEvent

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
)

class OverlayVisibilityPolicy(
    private val clickActivationWindowMillis: Long = 1_500L,
) {
    private var lastPotentialTextFieldClickPackage: String? = null
    private var lastPotentialTextFieldClickTime: Long = Long.MIN_VALUE

    fun decide(
        event: OverlayVisibilityEvent,
        overlayShown: Boolean,
    ): OverlayVisibilityDecision {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            rememberPotentialTextFieldClick(event)
        }

        if (event.sourceEditable == true && event.sourceFocused != false) {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED || followsSamePackageClick(event)) {
                return OverlayVisibilityDecision.SHOW
            }
        }

        if (event.focusedEditable == true) {
            return if (overlayShown) {
                OverlayVisibilityDecision.KEEP
            } else if (event.inputMethodEvent) {
                OverlayVisibilityDecision.SHOW
            } else if (followsSamePackageClick(event)) {
                OverlayVisibilityDecision.SHOW
            } else {
                OverlayVisibilityDecision.KEEP
            }
        }

        return OverlayVisibilityDecision.HIDE
    }

    private fun rememberPotentialTextFieldClick(event: OverlayVisibilityEvent) {
        if (event.sourceEditable != true) return
        lastPotentialTextFieldClickPackage = event.packageName
        lastPotentialTextFieldClickTime = event.eventTime
    }

    private fun followsSamePackageClick(event: OverlayVisibilityEvent): Boolean {
        val packageName = event.packageName ?: return false
        return packageName == lastPotentialTextFieldClickPackage &&
            event.eventTime - lastPotentialTextFieldClickTime in 0..clickActivationWindowMillis
    }
}
