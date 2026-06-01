package com.verbally.app.system

enum class RuntimeRepairTarget {
    MICROPHONE,
    OVERLAY,
    ACCESSIBILITY,
}

sealed interface DictationRuntimeReadinessDecision {
    data object Ready : DictationRuntimeReadinessDecision
    data class RepairBubble(val target: RuntimeRepairTarget) : DictationRuntimeReadinessDecision
    data class MainAppRecovery(val target: RuntimeRepairTarget) : DictationRuntimeReadinessDecision
}

class DictationRuntimeReadinessPolicy {
    fun decide(
        microphoneGranted: Boolean,
        overlayGranted: Boolean,
        accessibilityEnabled: Boolean,
    ): DictationRuntimeReadinessDecision =
        when {
            !overlayGranted -> DictationRuntimeReadinessDecision.MainAppRecovery(RuntimeRepairTarget.OVERLAY)
            !accessibilityEnabled -> DictationRuntimeReadinessDecision.MainAppRecovery(RuntimeRepairTarget.ACCESSIBILITY)
            !microphoneGranted -> DictationRuntimeReadinessDecision.RepairBubble(RuntimeRepairTarget.MICROPHONE)
            else -> DictationRuntimeReadinessDecision.Ready
        }
}
