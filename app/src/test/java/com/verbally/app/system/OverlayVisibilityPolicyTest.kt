package com.verbally.app.system

import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayVisibilityPolicyTest {
    @Test
    fun passiveFocusedEditableEventKeepsBubbleHidden() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                packageName = "jp.naver.line.android",
                focusedEditable = true,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.KEEP, decision)
    }

    @Test
    fun editableClickShowsBubbleImmediately() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_VIEW_CLICKED,
                packageName = "jp.naver.line.android",
                sourceEditable = true,
                sourceFocused = true,
                focusedEditable = true,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.SHOW, decision)
    }

    @Test
    fun samePackageNonEditableClickDoesNotActivateAutoFocusedInput() {
        val policy = OverlayVisibilityPolicy()

        policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_VIEW_CLICKED,
                packageName = "jp.naver.line.android",
                eventTime = 1_000L,
                sourceEditable = null,
                focusedEditable = null,
            ),
            overlayShown = false,
        )

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_VIEW_FOCUSED,
                packageName = "jp.naver.line.android",
                eventTime = 1_300L,
                sourceEditable = true,
                sourceFocused = true,
                focusedEditable = true,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.KEEP, decision)
    }

    @Test
    fun launcherClickDoesNotActivateWhatsappAutoFocusedInput() {
        val policy = OverlayVisibilityPolicy()

        policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_VIEW_CLICKED,
                packageName = "com.sec.android.app.launcher",
                eventTime = 1_000L,
                sourceEditable = null,
                focusedEditable = true,
            ),
            overlayShown = false,
        )

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_VIEW_FOCUSED,
                packageName = "com.whatsapp",
                eventTime = 1_100L,
                sourceEditable = true,
                sourceFocused = true,
                focusedEditable = true,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.KEEP, decision)
    }

    @Test
    fun keyboardEventsKeepShownBubbleVisibleWhileInputRemainsFocused() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                packageName = "com.google.android.inputmethod.latin",
                sourceEditable = false,
                sourceFocused = false,
                focusedEditable = true,
            ),
            overlayShown = true,
        )

        assertEquals(OverlayVisibilityDecision.KEEP, decision)
    }

    @Test
    fun keyboardEventShowsBubbleWhenFocusedInputIsAlreadyOpen() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                packageName = "com.google.android.inputmethod.latin",
                sourceEditable = false,
                sourceFocused = false,
                focusedEditable = true,
                inputMethodEvent = true,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.SHOW, decision)
    }

    @Test
    fun nonKeyboardSystemEventKeepsBubbleHiddenWhenFocusedInputIsStale() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                packageName = "com.android.systemui",
                sourceEditable = false,
                sourceFocused = false,
                focusedEditable = true,
                inputMethodEvent = false,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.KEEP, decision)
    }

    @Test
    fun nonEditableWindowHidesBubble() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                packageName = "com.sec.android.app.launcher",
                sourceEditable = false,
                sourceFocused = false,
                focusedEditable = false,
            ),
            overlayShown = true,
        )

        assertEquals(OverlayVisibilityDecision.HIDE, decision)
    }

    private fun event(
        type: Int,
        packageName: String,
        eventTime: Long = 1_000L,
        sourceEditable: Boolean? = false,
        sourceFocused: Boolean? = false,
        focusedEditable: Boolean? = false,
        inputMethodEvent: Boolean = false,
    ) = OverlayVisibilityEvent(
        eventType = type,
        packageName = packageName,
        eventTime = eventTime,
        sourceEditable = sourceEditable,
        sourceFocused = sourceFocused,
        focusedEditable = focusedEditable,
        inputMethodEvent = inputMethodEvent,
    )
}
