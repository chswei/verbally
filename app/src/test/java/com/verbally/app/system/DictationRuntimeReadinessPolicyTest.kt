package com.verbally.app.system

import org.junit.Assert.assertEquals
import org.junit.Test

class DictationRuntimeReadinessPolicyTest {
    private val policy = DictationRuntimeReadinessPolicy()

    @Test
    fun microphoneRevocationUsesRepairBubbleWhenOverlayAndAccessibilityRemainAvailable() {
        val decision = policy.decide(
            microphoneGranted = false,
            overlayGranted = true,
            accessibilityEnabled = true,
        )

        assertEquals(
            DictationRuntimeReadinessDecision.RepairBubble(RuntimeRepairTarget.MICROPHONE),
            decision,
        )
    }

    @Test
    fun overlayRevocationRequiresMainAppRecoveryBecauseBubbleCannotBeShown() {
        val decision = policy.decide(
            microphoneGranted = true,
            overlayGranted = false,
            accessibilityEnabled = true,
        )

        assertEquals(
            DictationRuntimeReadinessDecision.MainAppRecovery(RuntimeRepairTarget.OVERLAY),
            decision,
        )
    }

    @Test
    fun accessibilityRevocationRequiresMainAppRecovery() {
        val decision = policy.decide(
            microphoneGranted = true,
            overlayGranted = true,
            accessibilityEnabled = false,
        )

        assertEquals(
            DictationRuntimeReadinessDecision.MainAppRecovery(RuntimeRepairTarget.ACCESSIBILITY),
            decision,
        )
    }

    @Test
    fun allRuntimeDependenciesAvailableIsReady() {
        val decision = policy.decide(
            microphoneGranted = true,
            overlayGranted = true,
            accessibilityEnabled = true,
        )

        assertEquals(DictationRuntimeReadinessDecision.Ready, decision)
    }
}
