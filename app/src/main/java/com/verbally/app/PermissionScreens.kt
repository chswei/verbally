package com.verbally.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.verbally.app.permissions.PermissionAction
import com.verbally.app.permissions.AccessibilityPermissionAction
import com.verbally.app.permissions.PermissionGuidance
import com.verbally.app.permissions.PermissionSetupStep

@Composable
internal fun PermissionScreen(
    onPermissionsChanged: () -> Unit,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = onPermissionsChanged,
) {
    val context = LocalContext.current
    val microphoneGrantedMessage = stringResource(R.string.permission_microphone_granted)
    val microphoneDeniedMessage = stringResource(R.string.permission_microphone_denied)
    val permissionPrefs = remember {
        context.getSharedPreferences("verbally_permission_state", Context.MODE_PRIVATE)
    }
    var microphoneGranted by remember { mutableStateOf(isMicrophoneGranted(context)) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var accessibilityGranted by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var microphoneRequestedBefore by remember {
        mutableStateOf(permissionPrefs.getBoolean(KEY_MICROPHONE_REQUESTED, false))
    }
    var accessibilityDisclosureAccepted by remember {
        mutableStateOf(permissionPrefs.getBoolean(KEY_ACCESSIBILITY_DISCLOSURE_ACCEPTED, false))
    }
    fun refreshLocalPermissions() {
        microphoneGranted = isMicrophoneGranted(context)
        overlayGranted = Settings.canDrawOverlays(context)
        accessibilityGranted = isAccessibilityServiceEnabled(context)
        onPermissionsChanged()
    }
    PermissionResumeRefreshEffect(onRefresh = ::refreshLocalPermissions)
    val microphoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        microphoneGranted = granted
        microphoneRequestedBefore = true
        permissionPrefs.edit {
            putBoolean(KEY_MICROPHONE_REQUESTED, true)
        }
        refreshLocalPermissions()
        Toast.makeText(
            context,
            if (granted) microphoneGrantedMessage else microphoneDeniedMessage,
            Toast.LENGTH_LONG,
        ).show()
    }
    fun openMicrophoneSettings(currentMicrophoneGranted: Boolean) {
        when (
            PermissionGuidance.microphoneAction(
                isGranted = currentMicrophoneGranted,
                hasRequestedBefore = microphoneRequestedBefore,
            )
        ) {
            PermissionAction.ALREADY_GRANTED -> {
                Toast.makeText(context, microphoneGrantedMessage, Toast.LENGTH_SHORT).show()
            }
            PermissionAction.REQUEST_RUNTIME_PERMISSION -> {
                permissionPrefs.edit {
                    putBoolean(KEY_MICROPHONE_REQUESTED, true)
                }
                microphoneRequestedBefore = true
                microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            PermissionAction.OPEN_APP_DETAILS -> {
                openAppDetails(context)
            }
        }
    }
    fun continuePermissionSetup() {
        val currentMicrophoneGranted = isMicrophoneGranted(context)
        val currentOverlayGranted = Settings.canDrawOverlays(context)
        val currentAccessibilityGranted = isAccessibilityServiceEnabled(context)
        microphoneGranted = currentMicrophoneGranted
        overlayGranted = currentOverlayGranted
        accessibilityGranted = currentAccessibilityGranted
        when (
            PermissionGuidance.nextSetupStep(
                microphoneGranted = currentMicrophoneGranted,
                overlayGranted = currentOverlayGranted,
                accessibilityGranted = currentAccessibilityGranted,
            )
        ) {
            PermissionSetupStep.MICROPHONE -> openMicrophoneSettings(currentMicrophoneGranted)
            PermissionSetupStep.OVERLAY -> {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${context.packageName}".toUri(),
                    ),
                )
            }
            PermissionSetupStep.ACCESSIBILITY -> {
                when (
                    PermissionGuidance.accessibilityPermissionAction(
                        disclosureAccepted = accessibilityDisclosureAccepted,
                    )
                ) {
                    AccessibilityPermissionAction.SHOW_DISCLOSURE -> Unit
                    AccessibilityPermissionAction.OPEN_SYSTEM_SETTINGS -> {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                }
            }
            PermissionSetupStep.COMPLETE -> {
                onComplete()
            }
        }
    }
    val nextStep = PermissionGuidance.nextSetupStep(
        microphoneGranted = microphoneGranted,
        overlayGranted = overlayGranted,
        accessibilityGranted = accessibilityGranted,
    )
    val primaryActionLabel = stringResource(permissionActionLabelRes(nextStep))
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PlainPageTopBar(stringResource(R.string.permissions_title))
        },
    ) { padding ->
        PermissionSetupContent(
            microphoneGranted = microphoneGranted,
            overlayGranted = overlayGranted,
            accessibilityGranted = accessibilityGranted,
            accessibilityDisclosureAccepted = accessibilityDisclosureAccepted,
            primaryActionLabel = primaryActionLabel,
            onContinue = { continuePermissionSetup() },
            onAcceptAccessibilityDisclosure = {
                permissionPrefs.edit {
                    putBoolean(KEY_ACCESSIBILITY_DISCLOSURE_ACCEPTED, true)
                }
                accessibilityDisclosureAccepted = true
            },
            onDeclineAccessibilityDisclosure = onComplete,
            onOpenAppDetails = { openAppDetails(context) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
fun PermissionSetupContent(
    microphoneGranted: Boolean,
    overlayGranted: Boolean,
    accessibilityGranted: Boolean,
    accessibilityDisclosureAccepted: Boolean = false,
    primaryActionLabel: String,
    onContinue: () -> Unit,
    onAcceptAccessibilityDisclosure: () -> Unit = {},
    onDeclineAccessibilityDisclosure: () -> Unit = {},
    onOpenAppDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = PermissionGuidance.nextSetupStep(
        microphoneGranted = microphoneGranted,
        overlayGranted = overlayGranted,
        accessibilityGranted = accessibilityGranted,
    )
    val details = PermissionStepDetails.from(step)
    val showAccessibilityDisclosure = PermissionGuidance.shouldShowAccessibilityDisclosure(
        step = step,
        disclosureAccepted = accessibilityDisclosureAccepted,
    )
    Column(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (step != PermissionSetupStep.COMPLETE) {
            PermissionStepCard(details = details)
        } else {
            Text(
                text = stringResource(details.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(details.subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (showAccessibilityDisclosure) {
            AccessibilityDisclosureCard(
                onAccept = onAcceptAccessibilityDisclosure,
                onDecline = onDeclineAccessibilityDisclosure,
            )
        } else if (step == PermissionSetupStep.ACCESSIBILITY) {
            ShortcutHintCard()
            TroubleshootingCard(onOpenAppDetails = onOpenAppDetails)
        }
        if (!showAccessibilityDisclosure) {
            AdaptiveActionButton(
                text = primaryActionLabel,
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
internal fun PermissionResumeRefreshEffect(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onRefresh: () -> Unit,
) {
    DisposableEffect(lifecycleOwner, onRefresh) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

private data class PermissionStepDetails(
    @param:StringRes val progressRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    @param:StringRes val descriptionRes: Int,
) {
    companion object {
        fun from(step: PermissionSetupStep): PermissionStepDetails = when (step) {
            PermissionSetupStep.MICROPHONE -> PermissionStepDetails(
                progressRes = R.string.permission_step_microphone_progress,
                titleRes = R.string.permission_step_microphone_title,
                subtitleRes = R.string.permission_step_microphone_subtitle,
                descriptionRes = R.string.permission_step_microphone_description,
            )
            PermissionSetupStep.OVERLAY -> PermissionStepDetails(
                progressRes = R.string.permission_step_overlay_progress,
                titleRes = R.string.permission_step_overlay_title,
                subtitleRes = R.string.permission_step_overlay_subtitle,
                descriptionRes = R.string.permission_step_overlay_description,
            )
            PermissionSetupStep.ACCESSIBILITY -> PermissionStepDetails(
                progressRes = R.string.permission_step_accessibility_progress,
                titleRes = R.string.permission_step_accessibility_title,
                subtitleRes = R.string.permission_step_accessibility_subtitle,
                descriptionRes = R.string.permission_step_accessibility_description,
            )
            PermissionSetupStep.COMPLETE -> PermissionStepDetails(
                progressRes = R.string.permission_step_complete_progress,
                titleRes = R.string.permission_step_complete_title,
                subtitleRes = R.string.permission_step_complete_subtitle,
                descriptionRes = R.string.permission_step_complete_description,
            )
        }
    }
}

@StringRes
private fun permissionActionLabelRes(step: PermissionSetupStep): Int = when (step) {
    PermissionSetupStep.MICROPHONE -> R.string.permission_action_microphone
    PermissionSetupStep.OVERLAY -> R.string.permission_action_settings
    PermissionSetupStep.ACCESSIBILITY -> R.string.permission_action_settings
    PermissionSetupStep.COMPLETE -> R.string.permission_action_complete
}

@Composable
private fun TroubleshootingCard(
    onOpenAppDetails: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.permission_troubleshooting_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.permission_restricted_settings_explanation),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall,
            )
            AdaptiveOutlinedActionButton(
                text = stringResource(R.string.open_app_info),
                onClick = onOpenAppDetails,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AccessibilityDisclosureCard(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.accessibility_disclosure_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.accessibility_disclosure_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            AdaptiveActionButton(
                text = stringResource(R.string.accessibility_disclosure_accept),
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth(),
            )
            AdaptiveOutlinedActionButton(
                text = stringResource(R.string.accessibility_disclosure_decline),
                onClick = onDecline,
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ShortcutHintCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.permission_shortcut_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.permission_shortcut_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun PermissionStepCard(details: PermissionStepDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = stringResource(details.progressRes),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = stringResource(details.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(details.subtitleRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(details.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlainPageTopBar(title: String) {
    Surface(
        modifier = Modifier.statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = ScreenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
internal fun PermissionBanner(
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.permission_banner_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = stringResource(R.string.permission_banner_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            AdaptiveActionButton(
                text = stringResource(R.string.permission_banner_button),
                onClick = onOpenPermissions,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private const val KEY_MICROPHONE_REQUESTED = "microphone_requested"
private const val KEY_ACCESSIBILITY_DISCLOSURE_ACCEPTED = "accessibility_disclosure_accepted"
internal fun hasRequiredPermissions(context: Context): Boolean =
    isMicrophoneGranted(context) &&
        Settings.canDrawOverlays(context) &&
        isAccessibilityServiceEnabled(context)
private fun isMicrophoneGranted(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED
private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedService = "${context.packageName}/${context.packageName}.system.VerballyAccessibilityService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false
    val splitter = TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabledServices)
    while (splitter.hasNext()) {
        if (splitter.next().equals(expectedService, ignoreCase = true)) return true
    }
    return false
}

private fun openAppDetails(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            "package:${context.packageName}".toUri(),
        ),
    )
}
