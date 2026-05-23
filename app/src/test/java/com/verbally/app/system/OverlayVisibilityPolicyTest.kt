package com.verbally.app.system

import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayVisibilityPolicyTest {
    @Test
    fun inputMethodVisibleShowsBubbleWithoutFocusedEditableMetadata() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOWS_CHANGED,
                packageName = "com.android.systemui",
                sourceEditable = false,
                sourceFocused = false,
                focusedEditable = null,
                inputMethodVisible = true,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.SHOW, decision)
    }

    @Test
    fun inputMethodVisibleKeepsShownBubbleVisible() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                packageName = "com.android.systemui",
                sourceEditable = false,
                sourceFocused = false,
                focusedEditable = false,
                inputMethodVisible = true,
            ),
            overlayShown = true,
        )

        assertEquals(OverlayVisibilityDecision.KEEP, decision)
    }

    @Test
    fun inputMethodHiddenHidesBubbleEvenWhenEditableFocusIsRetained() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOWS_CHANGED,
                packageName = "com.android.systemui",
                sourceEditable = false,
                sourceFocused = false,
                focusedEditable = true,
                inputMethodVisible = false,
            ),
            overlayShown = true,
        )

        assertEquals(OverlayVisibilityDecision.HIDE, decision)
    }

    @Test
    fun editableClickBeforeInputMethodOpensKeepsBubbleHidden() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_VIEW_CLICKED,
                packageName = "jp.naver.line.android",
                sourceEditable = true,
                sourceFocused = true,
                focusedEditable = true,
                inputMethodVisible = false,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.KEEP, decision)
    }

    @Test
    fun passiveFocusedEditableWithoutInputMethodKeepsBubbleHidden() {
        val policy = OverlayVisibilityPolicy()

        val decision = policy.decide(
            event = event(
                type = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                packageName = "jp.naver.line.android",
                sourceEditable = false,
                sourceFocused = false,
                focusedEditable = true,
                inputMethodVisible = false,
            ),
            overlayShown = false,
        )

        assertEquals(OverlayVisibilityDecision.KEEP, decision)
    }

    private fun event(
        type: Int,
        packageName: String,
        eventTime: Long = 1_000L,
        sourceEditable: Boolean? = false,
        sourceFocused: Boolean? = false,
        focusedEditable: Boolean? = false,
        inputMethodEvent: Boolean = false,
        inputMethodVisible: Boolean = false,
    ) = OverlayVisibilityEvent(
        eventType = type,
        packageName = packageName,
        eventTime = eventTime,
        sourceEditable = sourceEditable,
        sourceFocused = sourceFocused,
        focusedEditable = focusedEditable,
        inputMethodEvent = inputMethodEvent,
        inputMethodVisible = inputMethodVisible,
    )
}
