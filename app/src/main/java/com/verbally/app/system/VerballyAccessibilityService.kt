package com.verbally.app.system

import android.accessibilityservice.AccessibilityService
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.verbally.app.DictationCoordinator
import com.verbally.app.VerballyApplication
import com.verbally.app.audio.TemporaryAudioRecorder
import com.verbally.app.insertion.AccessibilityPasteTarget
import com.verbally.app.insertion.AndroidClipboardGateway
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.overlay.FloatingDictationOverlay
import com.verbally.app.overlay.OverlayState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VerballyAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val visibilityPolicy = OverlayVisibilityPolicy()
    private var overlay: FloatingDictationOverlay? = null
    private var appLabel: String? = null
    private val coordinator: DictationCoordinator by lazy {
        val container = (application as VerballyApplication).container
        DictationCoordinator(
            settingsRepository = container.settingsRepository,
            historyRepository = container.historyRepository,
            audioRecorder = TemporaryAudioRecorder(this),
            transcriptionClient = container.transcriptionClient,
            openAiCleanupClient = container.openAiCleanupClient,
            geminiCleanupClient = container.geminiCleanupClient,
            insertionFactory = {
                ClipboardPasteInserter(
                    clipboard = AndroidClipboardGateway(this),
                    pasteTarget = AccessibilityPasteTarget(this),
                )
            },
        )
    }

    override fun onServiceConnected() {
        overlay = FloatingDictationOverlay(
            context = this,
            onStart = { coordinator.startRecording() },
            onCancel = { coordinator.cancelRecording() },
            onConfirm = { processRecording() },
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val focused = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val source = event.source
        val defaultInputMethodPackage = defaultInputMethodPackage()
        val decision = visibilityPolicy.decide(
            event = OverlayVisibilityEvent(
                eventType = event.eventType,
                packageName = event.packageName?.toString(),
                eventTime = event.eventTime,
                sourceEditable = source?.isEditable,
                sourceFocused = source?.isFocused,
                focusedEditable = focused?.isEditable,
                inputMethodEvent = event.packageName?.toString() == defaultInputMethodPackage,
                inputMethodVisible = isInputMethodVisible(),
            ),
            overlayShown = overlay?.isShown == true,
        )

        when (decision) {
            OverlayVisibilityDecision.SHOW -> {
                appLabel = event.packageName?.toString()
                overlay?.show()
            }
            OverlayVisibilityDecision.HIDE -> overlay?.hide()
            OverlayVisibilityDecision.KEEP -> Unit
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        overlay?.hide()
        super.onDestroy()
    }

    private fun processRecording() {
        scope.launch {
            runCatching { coordinator.confirmRecording(appLabel) }
                .onSuccess { result ->
                    overlay?.setState(if (result.pasted) OverlayState.SUCCESS else OverlayState.ERROR)
                    overlay?.showMessage(result.message)
                }
                .onFailure { error ->
                    overlay?.setState(OverlayState.ERROR)
                    overlay?.showMessage(error.message ?: "發生錯誤")
                }
        }
    }

    private fun defaultInputMethodPackage(): String? =
        Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            ?.substringBefore("/")

    private fun isInputMethodVisible(): Boolean =
        windows.any { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }
}
