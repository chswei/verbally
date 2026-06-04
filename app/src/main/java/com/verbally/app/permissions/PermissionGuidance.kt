package com.verbally.app.permissions

enum class PermissionAction {
    ALREADY_GRANTED,
    REQUEST_RUNTIME_PERMISSION,
    OPEN_APP_DETAILS,
}

enum class PermissionSetupStep {
    MICROPHONE,
    OVERLAY,
    ACCESSIBILITY,
    COMPLETE,
}

enum class AccessibilityPermissionAction {
    SHOW_DISCLOSURE,
    OPEN_SYSTEM_SETTINGS,
}

object PermissionGuidance {
    val restrictedSettingsExplanation: String =
        "如果 Verbally 浮動聽寫在輔助使用清單顯示「由受限制的設定控管」，" +
            "請先打開 Verbally 的 App 資訊，點右上角選單，選擇「允許受限制的設定」，" +
            "再回到輔助使用開啟服務。"

    fun microphoneAction(
        isGranted: Boolean,
        hasRequestedBefore: Boolean,
    ): PermissionAction = when {
        isGranted -> PermissionAction.ALREADY_GRANTED
        hasRequestedBefore -> PermissionAction.OPEN_APP_DETAILS
        else -> PermissionAction.REQUEST_RUNTIME_PERMISSION
    }

    fun nextSetupStep(
        microphoneGranted: Boolean,
        overlayGranted: Boolean,
        accessibilityGranted: Boolean,
    ): PermissionSetupStep = when {
        !microphoneGranted -> PermissionSetupStep.MICROPHONE
        !overlayGranted -> PermissionSetupStep.OVERLAY
        !accessibilityGranted -> PermissionSetupStep.ACCESSIBILITY
        else -> PermissionSetupStep.COMPLETE
    }

    fun actionLabel(step: PermissionSetupStep): String = when (step) {
        PermissionSetupStep.MICROPHONE -> "開啟權限"
        PermissionSetupStep.OVERLAY -> "開啟設定"
        PermissionSetupStep.ACCESSIBILITY -> "開啟設定"
        PermissionSetupStep.COMPLETE -> "完成設定"
    }

    fun accessibilityPermissionAction(disclosureAccepted: Boolean): AccessibilityPermissionAction =
        if (disclosureAccepted) {
            AccessibilityPermissionAction.OPEN_SYSTEM_SETTINGS
        } else {
            AccessibilityPermissionAction.SHOW_DISCLOSURE
        }

    fun shouldShowAccessibilityDisclosure(
        step: PermissionSetupStep,
        disclosureAccepted: Boolean,
    ): Boolean = step == PermissionSetupStep.ACCESSIBILITY && !disclosureAccepted
}
