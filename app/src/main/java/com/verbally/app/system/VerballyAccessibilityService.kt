package com.verbally.app.system

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.LocaleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.verbally.app.DictationCoordinator
import com.verbally.app.DictationOutcome
import com.verbally.app.R
import com.verbally.app.VerballyApplication
import com.verbally.app.audio.TemporaryAudioRecorder
import com.verbally.app.insertion.AccessibilityTextInsertionTarget
import com.verbally.app.insertion.AndroidClipboardGateway
import com.verbally.app.insertion.ClipboardPasteInserter
import com.verbally.app.overlay.FloatingDictationOverlay
import com.verbally.app.overlay.LiveWaveformLevelSmoother
import com.verbally.app.overlay.OverlayUiState
import com.verbally.app.settings.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class VerballyAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val visibilityPolicy = OverlayVisibilityPolicy()
    private val sensitiveInputPolicy = SensitiveInputPolicy()
    private val runtimeReadinessPolicy = DictationRuntimeReadinessPolicy()
    private val waveformSmoother = LiveWaveformLevelSmoother()
    private var overlay: FloatingDictationOverlay? = null
    private var appLabel: String? = null
    private var waveformJob: Job? = null
    private var debugInsertReceiver: BroadcastReceiver? = null
    private val coordinator: DictationCoordinator by lazy {
        val container = (application as VerballyApplication).container
        DictationCoordinator(
            settingsRepository = container.settingsRepository,
            historyRepository = container.historyRepository,
            dictionaryRepository = container.dictionaryRepository,
            snippetRepository = container.snippetRepository,
            styleProfileRepository = container.styleProfileRepository,
            styleRuleRepository = container.styleRuleRepository,
            audioRecorder = TemporaryAudioRecorder(this),
            transcriptionRouter = container.transcriptionRouter,
            openAiCleanupClient = container.openAiCleanupClient,
            geminiCleanupClient = container.geminiCleanupClient,
            noRecordingMessage = getString(R.string.error_no_recording),
            defaultPromptLanguageFor = { language -> defaultPromptLanguageFor(language) },
            insertionFactory = {
                ClipboardPasteInserter(
                    clipboard = AndroidClipboardGateway(this),
                    directTextTarget = AccessibilityTextInsertionTarget(this),
                    directInsertMessage = getString(R.string.insert_success),
                    clipboardFallbackMessage = getString(R.string.insert_clipboard_fallback),
                )
            },
        )
    }

    override fun onServiceConnected() {
        overlay = FloatingDictationOverlay(
            context = this,
            onStart = {
                coordinator.startRecording()
                startWaveformUpdates()
            },
            onCancel = {
                stopWaveformUpdates()
                coordinator.cancelRecording()
            },
            onConfirm = {
                stopWaveformUpdates()
                processRecording()
            },
            onRepair = ::openRuntimeRepair,
        )
        registerDebugInsertReceiver()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val focused = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val source = event.source
        val packageName = event.packageName?.toString()
        val defaultInputMethodPackage = defaultInputMethodPackage()
        val sensitiveInput = sensitiveInputPolicy.isSensitive(
            SensitiveInputContext(
                packageName = packageName,
                isPassword = focused?.isPassword ?: source?.isPassword ?: false,
                inputType = focused?.inputType ?: source?.inputType ?: 0,
            ),
        )
        val decision = visibilityPolicy.decide(
            event = OverlayVisibilityEvent(
                eventType = event.eventType,
                packageName = packageName,
                eventTime = event.eventTime,
                sourceEditable = source?.isEditable,
                sourceFocused = source?.isFocused,
                focusedEditable = focused?.isEditable,
                inputMethodEvent = packageName == defaultInputMethodPackage,
                inputMethodVisible = isInputMethodVisible(),
                sensitiveInput = sensitiveInput,
            ),
            overlayShown = overlay?.isShown == true,
        )

        when (decision) {
            OverlayVisibilityDecision.SHOW -> handleShowDecision(packageName)
            OverlayVisibilityDecision.HIDE -> overlay?.hide()
            OverlayVisibilityDecision.KEEP -> Unit
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        stopWaveformUpdates()
        unregisterDebugInsertReceiver()
        overlay?.dispose()
        super.onDestroy()
    }

    private fun defaultPromptLanguageFor(language: AppLanguage): AppLanguage =
        AppLanguage.defaultPromptLanguageFor(
            selectedInterfaceLanguage = language,
            systemLanguageTag = getSystemService(LocaleManager::class.java).systemLocales[0]?.toLanguageTag(),
        )

    private fun processRecording() {
        scope.launch {
            runCatching { coordinator.confirmRecording(appLabel) }
                .onSuccess { result ->
                    when (result) {
                        is DictationOutcome.Inserted,
                        DictationOutcome.NoDictatedContent -> reportProcessingSuccess()
                        is DictationOutcome.ClipboardFallback -> reportProcessingFailure(result.message)
                        is DictationOutcome.Failure -> reportProcessingFailure(result.message)
                    }
                }
                .onFailure { error ->
                    reportProcessingFailure(error.message ?: getString(R.string.error_generic))
                }
        }
    }

    private fun reportProcessingSuccess() {
        val currentOverlay = overlay ?: return
        DictationFeedbackReporter(
            overlay = FloatingDictationOverlayFeedback(currentOverlay),
            userMessages = ToastUserMessageSink(this),
        ).reportSuccess()
    }

    private fun reportProcessingFailure(message: String) {
        val currentOverlay = overlay
        if (currentOverlay == null) {
            ToastUserMessageSink(this).show(message)
            return
        }
        DictationFeedbackReporter(
            overlay = FloatingDictationOverlayFeedback(currentOverlay),
            userMessages = ToastUserMessageSink(this),
        ).reportFailure(message)
    }

    private fun handleShowDecision(packageName: String?) {
        appLabel = packageName
        when (
            val readiness = runtimeReadinessPolicy.decide(
                microphoneGranted = hasRecordAudioPermission(),
                overlayGranted = Settings.canDrawOverlays(this),
                accessibilityEnabled = true,
            )
        ) {
            DictationRuntimeReadinessDecision.Ready -> {
                if (overlay?.currentState == OverlayUiState.REPAIR) {
                    overlay?.setState(OverlayUiState.READY)
                }
                overlay?.show()
            }
            is DictationRuntimeReadinessDecision.RepairBubble -> {
                overlay?.showRepair(readiness.target, repairMessageFor(readiness.target))
            }
            is DictationRuntimeReadinessDecision.MainAppRecovery -> overlay?.hide()
        }
    }

    private fun hasRecordAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    private fun repairMessageFor(target: RuntimeRepairTarget): String =
        when (target) {
            RuntimeRepairTarget.MICROPHONE -> getString(R.string.overlay_repair_microphone)
            RuntimeRepairTarget.OVERLAY -> getString(R.string.overlay_repair_overlay)
            RuntimeRepairTarget.ACCESSIBILITY -> getString(R.string.overlay_repair_accessibility)
        }

    private fun openRuntimeRepair(target: RuntimeRepairTarget) {
        val intent = when (target) {
            RuntimeRepairTarget.MICROPHONE -> Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                "package:$packageName".toUri(),
            )
            RuntimeRepairTarget.OVERLAY -> Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri(),
            )
            RuntimeRepairTarget.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { startActivity(intent) }
    }

    private fun startWaveformUpdates() {
        waveformJob?.cancel()
        waveformSmoother.reset()
        overlay?.setWaveformLevel(0f)
        waveformJob = scope.launch {
            while (isActive) {
                val level = waveformSmoother.update(coordinator.currentAmplitude())
                overlay?.setWaveformLevel(level)
                delay(50)
            }
        }
    }

    private fun stopWaveformUpdates() {
        waveformJob?.cancel()
        waveformJob = null
        overlay?.setWaveformLevel(waveformSmoother.reset())
    }

    private fun defaultInputMethodPackage(): String? =
        Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            ?.substringBefore("/")

    private fun isInputMethodVisible(): Boolean =
        windows.any { it.type == AccessibilityWindowInfo.TYPE_INPUT_METHOD }

    private fun registerDebugInsertReceiver() {
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) == 0) return
        if (debugInsertReceiver != null) return
        debugInsertReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != DEBUG_INSERT_TEXT_ACTION) return
                val text = intent.getStringExtra(DEBUG_INSERT_TEXT_EXTRA).orEmpty()
                if (text.isBlank()) return
                scope.launch {
                    val result = ClipboardPasteInserter(
                        clipboard = AndroidClipboardGateway(this@VerballyAccessibilityService),
                        directTextTarget = AccessibilityTextInsertionTarget(this@VerballyAccessibilityService),
                        directInsertMessage = getString(R.string.insert_success),
                        clipboardFallbackMessage = getString(R.string.insert_clipboard_fallback),
                    ).insert(text)
                    Log.d(DEBUG_TAG, "debug insert result=$result")
                }
            }
        }
        registerReceiver(
            debugInsertReceiver,
            IntentFilter(DEBUG_INSERT_TEXT_ACTION),
            Context.RECEIVER_EXPORTED,
        )
    }

    private fun unregisterDebugInsertReceiver() {
        debugInsertReceiver?.let { runCatching { unregisterReceiver(it) } }
        debugInsertReceiver = null
    }

    companion object {
        const val DEBUG_INSERT_TEXT_ACTION = "com.verbally.app.DEBUG_INSERT_TEXT"
        const val DEBUG_INSERT_TEXT_EXTRA = "text"
        private const val DEBUG_TAG = "VerballyDebugInsert"
    }
}
