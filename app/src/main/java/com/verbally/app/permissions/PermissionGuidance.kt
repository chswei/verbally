package com.verbally.app.permissions

enum class PermissionAction {
    ALREADY_GRANTED,
    REQUEST_RUNTIME_PERMISSION,
    OPEN_APP_DETAILS,
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
}
