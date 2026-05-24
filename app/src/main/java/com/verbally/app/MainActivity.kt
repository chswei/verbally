package com.verbally.app

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.permissions.PermissionAction
import com.verbally.app.permissions.PermissionGuidance
import com.verbally.app.permissions.PermissionSetupStep
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.CleanupProvider

private val VerballyBrandBlue = Color(0xFF14233A)
private val VerballySoftBlue = Color(0xFFE6EDF6)
private val VerballyPageBackground = Color(0xFFF7F9FC)

private val VerballyColorScheme = lightColorScheme(
    primary = VerballyBrandBlue,
    onPrimary = Color.White,
    primaryContainer = VerballySoftBlue,
    onPrimaryContainer = VerballyBrandBlue,
    secondary = Color(0xFF526275),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6ECF3),
    onSecondaryContainer = Color(0xFF111C2B),
    background = VerballyPageBackground,
    onBackground = Color(0xFF171C22),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF171C22),
    surfaceVariant = Color(0xFFE7ECF3),
    onSurfaceVariant = Color(0xFF465464),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as VerballyApplication).container
        setContent {
            VerballyTheme {
                VerballyApp(container)
            }
        }
    }
}

@Composable
private fun VerballyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VerballyColorScheme,
        content = content,
    )
}

@Composable
fun VerballyApp(container: VerballyContainer) {
    val context = LocalContext.current
    var selectedDestination by remember { mutableStateOf(AppDestination.HISTORY) }
    var settingsPage by remember { mutableStateOf(SettingsPage.OVERVIEW) }
    var permissionsReady by remember { mutableStateOf(hasRequiredPermissions(context)) }
    var showingPermissions by remember { mutableStateOf(!permissionsReady) }

    fun refreshPermissions() {
        permissionsReady = hasRequiredPermissions(context)
        if (permissionsReady) showingPermissions = false
    }

    if (showingPermissions) {
        PermissionScreen(
            onPermissionsChanged = { refreshPermissions() },
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    VerballyAppScaffold(
        permissionsReady = permissionsReady,
        selectedDestination = selectedDestination,
        onDestinationSelected = {
            refreshPermissions()
            selectedDestination = it
            if (it != AppDestination.SETTINGS) settingsPage = SettingsPage.OVERVIEW
        },
        onOpenPermissions = { showingPermissions = true },
        historyContent = {
            HistoryScreen(
                container = container,
                modifier = Modifier.fillMaxSize(),
            )
        },
        settingsContent = {
            SettingsScreen(
                container = container,
                page = settingsPage,
                onPageChange = { settingsPage = it },
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}

enum class AppDestination(val label: String, val marker: String) {
    HISTORY("歷史", "歷"),
    SETTINGS("設定", "設"),
}

private enum class SettingsPage {
    OVERVIEW,
    TRANSCRIPTION,
    CLEANUP,
}

@Composable
fun VerballyAppScaffold(
    permissionsReady: Boolean,
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    onOpenPermissions: () -> Unit,
    historyContent: @Composable () -> Unit,
    settingsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AppDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = { onDestinationSelected(destination) },
                        icon = { Text(destination.marker, fontWeight = FontWeight.SemiBold) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (!permissionsReady) {
                PermissionBanner(
                    onOpenPermissions = onOpenPermissions,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                when (selectedDestination) {
                    AppDestination.HISTORY -> historyContent()
                    AppDestination.SETTINGS -> settingsContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionScreen(
    onPermissionsChanged: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val permissionPrefs = remember {
        context.getSharedPreferences("verbally_permission_state", Context.MODE_PRIVATE)
    }
    var microphoneGranted by remember { mutableStateOf(isMicrophoneGranted(context)) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var accessibilityGranted by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var microphoneRequestedBefore by remember {
        mutableStateOf(permissionPrefs.getBoolean(KEY_MICROPHONE_REQUESTED, false))
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
        permissionPrefs.edit().putBoolean(KEY_MICROPHONE_REQUESTED, true).apply()
        refreshLocalPermissions()
        Toast.makeText(
            context,
            if (granted) "麥克風權限已開啟" else "麥克風權限尚未開啟，請到 App 資訊中允許。",
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
                Toast.makeText(context, "麥克風權限已開啟", Toast.LENGTH_SHORT).show()
            }
            PermissionAction.REQUEST_RUNTIME_PERMISSION -> {
                permissionPrefs.edit().putBoolean(KEY_MICROPHONE_REQUESTED, true).apply()
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
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            }
            PermissionSetupStep.ACCESSIBILITY -> {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            PermissionSetupStep.COMPLETE -> {
                onPermissionsChanged()
            }
        }
    }
    val primaryActionLabel = when (
        PermissionGuidance.nextSetupStep(
            microphoneGranted = microphoneGranted,
            overlayGranted = overlayGranted,
            accessibilityGranted = accessibilityGranted,
        )
    ) {
        PermissionSetupStep.MICROPHONE -> PermissionGuidance.actionLabel(PermissionSetupStep.MICROPHONE)
        PermissionSetupStep.OVERLAY -> PermissionGuidance.actionLabel(PermissionSetupStep.OVERLAY)
        PermissionSetupStep.ACCESSIBILITY -> PermissionGuidance.actionLabel(PermissionSetupStep.ACCESSIBILITY)
        PermissionSetupStep.COMPLETE -> PermissionGuidance.actionLabel(PermissionSetupStep.COMPLETE)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("權限設定") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        PermissionSetupContent(
            microphoneGranted = microphoneGranted,
            overlayGranted = overlayGranted,
            accessibilityGranted = accessibilityGranted,
            primaryActionLabel = primaryActionLabel,
            onContinue = { continuePermissionSetup() },
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
    primaryActionLabel: String,
    onContinue: () -> Unit,
    onOpenAppDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val step = PermissionGuidance.nextSetupStep(
        microphoneGranted = microphoneGranted,
        overlayGranted = overlayGranted,
        accessibilityGranted = accessibilityGranted,
    )
    val details = PermissionStepDetails.from(step)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (step != PermissionSetupStep.COMPLETE) {
            PermissionStepCard(details = details)
        } else {
            Text(
                text = details.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = details.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (step == PermissionSetupStep.ACCESSIBILITY) {
            ShortcutHintCard()
            TroubleshootingCard(onOpenAppDetails = onOpenAppDetails)
        }
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(primaryActionLabel)
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
    val progress: String,
    val title: String,
    val subtitle: String,
    val description: String,
) {
    companion object {
        fun from(step: PermissionSetupStep): PermissionStepDetails = when (step) {
            PermissionSetupStep.MICROPHONE -> PermissionStepDetails(
                progress = "步驟 1/3",
                title = "允許錄音",
                subtitle = "讓 Verbally 把你的語音轉成文字。",
                description = "系統會跳出授權視窗，選擇允許後就會回到下一步。",
            )
            PermissionSetupStep.OVERLAY -> PermissionStepDetails(
                progress = "步驟 2/3",
                title = "允許浮動視窗",
                subtitle = "讓聽寫按鈕出現在其他 app 的文字框旁。",
                description = "在系統設定中開啟「允許顯示在其他應用程式上方」。",
            )
            PermissionSetupStep.ACCESSIBILITY -> PermissionStepDetails(
                progress = "步驟 3/3",
                title = "開啟輔助使用",
                subtitle = "這一步讓 Verbally 能偵測文字框，並把整理後文字貼到游標位置。",
                description = "到「已安裝的應用程式」找到 Verbally 浮動聽寫並開啟。",
            )
            PermissionSetupStep.COMPLETE -> PermissionStepDetails(
                progress = "完成",
                title = "權限都完成了",
                subtitle = "按下完成設定即可進入 Verbally。",
                description = "所有必要權限已開啟。",
            )
        }
    }
}

@Composable
private fun TroubleshootingCard(
    onOpenAppDetails: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "如果輔助使用被系統鎖住",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = PermissionGuidance.restrictedSettingsExplanation,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedButton(
                onClick = onOpenAppDetails,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("開啟 App 資訊")
            }
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "捷徑不用開",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "看到「Verbally 捷徑」時保持關閉即可。",
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
            modifier = Modifier.padding(18.dp),
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
                        text = details.progress,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = details.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = details.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = details.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PermissionBanner(
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "權限需要重新確認",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "缺少權限時，浮動聽寫按鈕可能不會出現。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Button(onClick = onOpenPermissions) {
                Text("補開權限")
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, positive: Boolean) {
    Surface(
        color = if (positive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SettingsScreen(
    container: VerballyContainer,
    page: SettingsPage,
    onPageChange: (SettingsPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var settings by remember { mutableStateOf(container.settingsRepository.load()) }
    val context = LocalContext.current
    val saveSettings = {
        container.settingsRepository.save(settings)
        Toast.makeText(context, "設定已儲存", Toast.LENGTH_SHORT).show()
    }

    when (page) {
        SettingsPage.OVERVIEW -> SettingsScreenContent(
            settings = settings,
            onSettingsChange = { settings = it },
            onSave = saveSettings,
            onClearHistory = {
                container.historyRepository.clear()
                Toast.makeText(context, "歷史已清空", Toast.LENGTH_SHORT).show()
            },
            onOpenTranscriptionSettings = { onPageChange(SettingsPage.TRANSCRIPTION) },
            onOpenCleanupSettings = { onPageChange(SettingsPage.CLEANUP) },
            modifier = modifier,
        )
        SettingsPage.TRANSCRIPTION -> TranscriptionSettingsScreenContent(
            settings = settings,
            onSettingsChange = { settings = it },
            onSave = saveSettings,
            onBack = { onPageChange(SettingsPage.OVERVIEW) },
            modifier = modifier,
        )
        SettingsPage.CLEANUP -> CleanupSettingsScreenContent(
            settings = settings,
            onSettingsChange = { settings = it },
            onSave = saveSettings,
            onBack = { onPageChange(SettingsPage.OVERVIEW) },
            modifier = modifier,
        )
    }
}

@Composable
fun SettingsScreenContent(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onSave: () -> Unit,
    onClearHistory: () -> Unit,
    onOpenTranscriptionSettings: () -> Unit,
    onOpenCleanupSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(title = "設定", subtitle = "調整轉錄、第二層處理與本機資料。")
        SectionLabel("API")
        SettingsListItem(
            title = "Transcribe",
            subtitle = "OpenAI API Key、轉錄模型",
            onClick = onOpenTranscriptionSettings,
        )
        SettingsListItem(
            title = "第二層處理",
            subtitle = "${settings.cleanupProvider.label}、整理模型與對應 API Key",
            onClick = onOpenCleanupSettings,
        )
        SectionLabel("資料")
        SettingsListItem(
            title = "清空歷史",
            subtitle = "刪除本機儲存的聽寫紀錄",
            onClick = onClearHistory,
        )
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun TranscriptionSettingsScreenContent(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(
            title = "Transcribe",
            subtitle = "OpenAI 語音轉文字設定。",
            onBack = onBack,
        )
        SecretField("OpenAI API Key", settings.openAiApiKey) {
            onSettingsChange(settings.copy(openAiApiKey = it))
        }
        OutlinedTextField(
            value = settings.transcriptionModel,
            onValueChange = { onSettingsChange(settings.copy(transcriptionModel = it)) },
            label = { Text("OpenAI 轉錄模型") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("儲存設定")
        }
    }
}

@Composable
fun CleanupSettingsScreenContent(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(
            title = "第二層處理",
            subtitle = "選擇文字整理服務與模型。",
            onBack = onBack,
        )
        SectionLabel("整理 Provider")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProviderButton("OpenAI", settings.cleanupProvider == CleanupProvider.OPENAI) {
                onSettingsChange(settings.copy(cleanupProvider = CleanupProvider.OPENAI))
            }
            ProviderButton("Gemini", settings.cleanupProvider == CleanupProvider.GEMINI) {
                onSettingsChange(settings.copy(cleanupProvider = CleanupProvider.GEMINI))
            }
        }
        OutlinedTextField(
            value = settings.openAiCleanupModel,
            onValueChange = { onSettingsChange(settings.copy(openAiCleanupModel = it)) },
            label = { Text("OpenAI 整理模型") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        SecretField("Gemini API Key", settings.geminiApiKey) {
            onSettingsChange(settings.copy(geminiApiKey = it))
        }
        OutlinedTextField(
            value = settings.geminiCleanupModel,
            onValueChange = { onSettingsChange(settings.copy(geminiCleanupModel = it)) },
            label = { Text("Gemini 整理模型") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("儲存設定")
        }
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (onBack != null) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 4.dp)) {
                    Text("‹")
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 6.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SettingsListItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        ListItem(
            headlineContent = {
                Text(title, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = {
                Text(subtitle)
            },
            trailingContent = {
                Text("›", style = MaterialTheme.typography.titleLarge)
            },
            colors = androidx.compose.material3.ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun SecretField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ProviderButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        TextButton(onClick = onClick) { Text(label) }
    }
}

@Composable
private fun HistoryScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.historyRepository.list()) }
    val context = LocalContext.current

    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader(title = "歷史", subtitle = "最近 100 筆聽寫結果只保存在這台裝置。")
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                entries = container.historyRepository.search(it)
            },
            label = { Text("搜尋歷史") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(entries, key = { it.id }) { entry ->
                HistoryItem(
                    entry = entry,
                    onCopy = {
                        copyText(context, entry.cleanedText)
                        Toast.makeText(context, "已複製", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = {
                        container.historyRepository.delete(entry.id)
                        entries = container.historyRepository.search(query)
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    entry: DictationHistoryEntry,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp),
        ) {
            Text(entry.cleanedText, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${entry.provider} / ${entry.cleanupModel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) { Text("複製") }
                TextButton(onClick = onDelete) { Text("刪除") }
            }
        }
    }
}

private val CleanupProvider.label: String
    get() = when (this) {
        CleanupProvider.OPENAI -> "OpenAI"
        CleanupProvider.GEMINI -> "Gemini"
    }

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Verbally", text))
}

private const val KEY_MICROPHONE_REQUESTED = "microphone_requested"

private fun hasRequiredPermissions(context: Context): Boolean =
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
            Uri.parse("package:${context.packageName}"),
        ),
    )
}
