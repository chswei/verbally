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
}
