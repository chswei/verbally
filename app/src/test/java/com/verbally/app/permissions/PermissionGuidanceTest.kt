package com.verbally.app.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionGuidanceTest {
    @Test
    fun microphoneActionRequestsPermissionBeforeFirstAttempt() {
        val action = PermissionGuidance.microphoneAction(
            isGranted = false,
            hasRequestedBefore = false,
        )

        assertEquals(PermissionAction.REQUEST_RUNTIME_PERMISSION, action)
    }

    @Test
    fun microphoneActionOpensAppSettingsAfterPreviousFailedRequest() {
        val action = PermissionGuidance.microphoneAction(
            isGranted = false,
            hasRequestedBefore = true,
        )

        assertEquals(PermissionAction.OPEN_APP_DETAILS, action)
    }

    @Test
    fun restrictedSettingsTextExplainsManualAppInfoStep() {
        val text = PermissionGuidance.restrictedSettingsExplanation

        assertTrue(text.contains("App 資訊"))
        assertTrue(text.contains("允許受限制的設定"))
        assertTrue(text.contains("輔助使用"))
    }

    @Test
    fun nextSetupStepStartsWithMicrophone() {
        val step = PermissionGuidance.nextSetupStep(
            microphoneGranted = false,
            overlayGranted = false,
            accessibilityGranted = false,
        )

        assertEquals(PermissionSetupStep.MICROPHONE, step)
    }

    @Test
    fun nextSetupStepMovesToOverlayAfterMicrophone() {
        val step = PermissionGuidance.nextSetupStep(
            microphoneGranted = true,
            overlayGranted = false,
            accessibilityGranted = false,
        )

        assertEquals(PermissionSetupStep.OVERLAY, step)
    }

    @Test
    fun nextSetupStepMovesToAccessibilityAfterOverlay() {
        val step = PermissionGuidance.nextSetupStep(
            microphoneGranted = true,
            overlayGranted = true,
            accessibilityGranted = false,
        )

        assertEquals(PermissionSetupStep.ACCESSIBILITY, step)
    }

    @Test
    fun nextSetupStepCompletesWhenAllPermissionsAreGranted() {
        val step = PermissionGuidance.nextSetupStep(
            microphoneGranted = true,
            overlayGranted = true,
            accessibilityGranted = true,
        )

        assertEquals(PermissionSetupStep.COMPLETE, step)
    }

    @Test
    fun setupStepActionLabelsDescribeCurrentPermission() {
        assertEquals("開啟權限", PermissionGuidance.actionLabel(PermissionSetupStep.MICROPHONE))
        assertEquals("開啟設定", PermissionGuidance.actionLabel(PermissionSetupStep.OVERLAY))
        assertEquals("開啟設定", PermissionGuidance.actionLabel(PermissionSetupStep.ACCESSIBILITY))
        assertEquals("完成設定", PermissionGuidance.actionLabel(PermissionSetupStep.COMPLETE))
    }

    @Test
    fun accessibilitySetupRequiresDisclosureConsentBeforeOpeningSettings() {
        assertEquals(
            AccessibilityPermissionAction.SHOW_DISCLOSURE,
            PermissionGuidance.accessibilityPermissionAction(disclosureAccepted = false),
        )
        assertEquals(
            AccessibilityPermissionAction.OPEN_SYSTEM_SETTINGS,
            PermissionGuidance.accessibilityPermissionAction(disclosureAccepted = true),
        )
    }

    @Test
    fun accessibilityDisclosureOnlyAppearsForUnacceptedAccessibilityStep() {
        assertTrue(
            PermissionGuidance.shouldShowAccessibilityDisclosure(
                step = PermissionSetupStep.ACCESSIBILITY,
                disclosureAccepted = false,
            ),
        )
        assertEquals(
            false,
            PermissionGuidance.shouldShowAccessibilityDisclosure(
                step = PermissionSetupStep.OVERLAY,
                disclosureAccepted = false,
            ),
        )
        assertEquals(
            false,
            PermissionGuidance.shouldShowAccessibilityDisclosure(
                step = PermissionSetupStep.ACCESSIBILITY,
                disclosureAccepted = true,
            ),
        )
    }
}
