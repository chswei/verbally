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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.permissions.PermissionAction
import com.verbally.app.permissions.PermissionGuidance
import com.verbally.app.settings.CleanupProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as VerballyApplication).container
        setContent {
            MaterialTheme {
                VerballyApp(container)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerballyApp(container: VerballyContainer) {
    var selectedTab by remember { mutableStateOf(AppTab.ONBOARDING) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Verbally") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { selectedTab = AppTab.ONBOARDING }) { Text("權限") }
                TextButton(onClick = { selectedTab = AppTab.SETTINGS }) { Text("設定") }
                TextButton(onClick = { selectedTab = AppTab.HISTORY }) { Text("歷史") }
            }
            when (selectedTab) {
                AppTab.ONBOARDING -> PermissionScreen()
                AppTab.SETTINGS -> SettingsScreen(container)
                AppTab.HISTORY -> HistoryScreen(container)
            }
        }
    }
}

private enum class AppTab {
    ONBOARDING,
    SETTINGS,
    HISTORY,
}

@Composable
private fun PermissionScreen() {
    val context = LocalContext.current
    val permissionPrefs = remember {
        context.getSharedPreferences("verbally_permission_state", Context.MODE_PRIVATE)
    }
    var microphoneGranted by remember { mutableStateOf(isMicrophoneGranted(context)) }
    var microphoneRequestedBefore by remember {
        mutableStateOf(permissionPrefs.getBoolean(KEY_MICROPHONE_REQUESTED, false))
    }
    val microphoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        microphoneGranted = granted
        microphoneRequestedBefore = true
        permissionPrefs.edit().putBoolean(KEY_MICROPHONE_REQUESTED, true).apply()
        Toast.makeText(
            context,
            if (granted) "麥克風權限已開啟" else "麥克風權限尚未開啟，請到 App 資訊中允許。",
            Toast.LENGTH_LONG,
        ).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("啟用浮動聽寫前，請開啟三個權限。")
        Text("麥克風：${if (microphoneGranted) "已開啟" else "尚未開啟"}")
        Button(onClick = {
            when (
                PermissionGuidance.microphoneAction(
                    isGranted = microphoneGranted,
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
        }) {
            Text(if (microphoneRequestedBefore && !microphoneGranted) "開啟 App 麥克風設定" else "開啟麥克風權限")
        }
        Text("浮動視窗：${if (Settings.canDrawOverlays(context)) "已開啟" else "尚未開啟"}")
        Button(onClick = {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                ),
            )
        }) {
            Text("開啟浮動視窗權限")
        }
        Text(PermissionGuidance.restrictedSettingsExplanation)
        Button(onClick = { openAppDetails(context) }) {
            Text("開啟 App 資訊")
        }
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
            Text("開啟輔助使用權限")
        }
        Text("輔助使用只用來偵測目前文字框、顯示浮動按鈕，並把整理後文字貼到游標位置。")
    }
}

@Composable
private fun SettingsScreen(container: VerballyContainer) {
    var settings by remember { mutableStateOf(container.settingsRepository.load()) }
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SecretField("OpenAI API Key", settings.openAiApiKey) {
            settings = settings.copy(openAiApiKey = it)
        }
        SecretField("Gemini API Key", settings.geminiApiKey) {
            settings = settings.copy(geminiApiKey = it)
        }
        OutlinedTextField(
            value = settings.transcriptionModel,
            onValueChange = { settings = settings.copy(transcriptionModel = it) },
            label = { Text("OpenAI 轉錄模型") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = settings.openAiCleanupModel,
            onValueChange = { settings = settings.copy(openAiCleanupModel = it) },
            label = { Text("OpenAI 整理模型") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = settings.geminiCleanupModel,
            onValueChange = { settings = settings.copy(geminiCleanupModel = it) },
            label = { Text("Gemini 整理模型") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProviderButton("OpenAI", settings.cleanupProvider == CleanupProvider.OPENAI) {
                settings = settings.copy(cleanupProvider = CleanupProvider.OPENAI)
            }
            ProviderButton("Gemini", settings.cleanupProvider == CleanupProvider.GEMINI) {
                settings = settings.copy(cleanupProvider = CleanupProvider.GEMINI)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                container.settingsRepository.save(settings)
                Toast.makeText(context, "設定已儲存", Toast.LENGTH_SHORT).show()
            }) {
                Text("儲存設定")
            }
            TextButton(onClick = {
                container.historyRepository.clear()
                Toast.makeText(context, "歷史已清空", Toast.LENGTH_SHORT).show()
            }) {
                Text("清空歷史")
            }
        }
    }
}

@Composable
private fun SecretField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
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
private fun HistoryScreen(container: VerballyContainer) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.historyRepository.list()) }
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                entries = container.historyRepository.search(it)
            },
            label = { Text("搜尋歷史") },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = {
            container.historyRepository.clear()
            entries = emptyList()
        }) {
            Text("清空全部")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text(entry.cleanedText)
        Text("${entry.provider} / ${entry.cleanupModel}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onCopy) { Text("複製/重貼") }
            TextButton(onClick = onDelete) { Text("刪除") }
        }
        HorizontalDivider()
    }
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Verbally", text))
}

private const val KEY_MICROPHONE_REQUESTED = "microphone_requested"

private fun isMicrophoneGranted(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO,
    ) == PackageManager.PERMISSION_GRANTED

private fun openAppDetails(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        ),
    )
}
