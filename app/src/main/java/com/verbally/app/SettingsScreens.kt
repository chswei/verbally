package com.verbally.app

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.verbally.app.providers.ProviderKeyTestResult
import com.verbally.app.providers.ProviderKeyTester
import com.verbally.app.history.HistoryRetentionMode
import com.verbally.app.settings.AppLanguage
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.AppThemeMode
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.ModelOptions
import com.verbally.app.settings.TranscriptionProvider
import com.verbally.app.settings.cleanupModelOptionLabel
import com.verbally.app.settings.cleanupPromptForDisplay
import com.verbally.app.settings.transcriptionModelOptionLabel
import com.verbally.app.settings.withCleanupModelOption
import com.verbally.app.settings.withCleanupPromptEdited
import com.verbally.app.settings.withDefaultCleanupPromptLanguage
import com.verbally.app.settings.withDefaultCleanupPromptRestored
import com.verbally.app.settings.withInterfaceLanguage
import com.verbally.app.settings.withTranscriptionModelOption
import kotlinx.coroutines.launch

@Composable
internal fun SettingsScreen(
    container: VerballyContainer,
    savedSettings: AppSettings,
    onSettingsSaved: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var settings by remember(savedSettings) { mutableStateOf(savedSettings) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsSavedMessage = stringResource(R.string.settings_saved)
    var transcriptionTestState by remember { mutableStateOf(ApiKeyTestUiState()) }
    var cleanupTestState by remember { mutableStateOf(ApiKeyTestUiState()) }
    val saveSettings = {
        container.settingsRepository.save(settings)
        onSettingsSaved(settings)
        Toast.makeText(context, settingsSavedMessage, Toast.LENGTH_SHORT).show()
    }
    fun updateSettings(next: AppSettings) {
        settings = next
        transcriptionTestState = ApiKeyTestUiState()
        cleanupTestState = ApiKeyTestUiState()
    }
    fun testTranscriptionKey(tester: ProviderKeyTester) {
        transcriptionTestState = ApiKeyTestUiState(isTesting = true)
        scope.launch {
            val result = tester.testTranscription(settings)
            transcriptionTestState = result.toApiKeyTestUiState(context)
        }
    }
    fun testCleanupKey(tester: ProviderKeyTester) {
        cleanupTestState = ApiKeyTestUiState(isTesting = true)
        scope.launch {
            val result = tester.testCleanup(settings)
            cleanupTestState = result.toApiKeyTestUiState(context)
        }
    }
    SettingsScreenContent(
        settings = settings,
        onSettingsChange = ::updateSettings,
        onSave = saveSettings,
        transcriptionTestState = transcriptionTestState,
        cleanupTestState = cleanupTestState,
        onTestTranscriptionApiKey = { testTranscriptionKey(container.providerKeyTester) },
        onTestCleanupApiKey = { testCleanupKey(container.providerKeyTester) },
        modifier = modifier,
    )
}

private fun ProviderKeyTestResult.toApiKeyTestUiState(context: Context): ApiKeyTestUiState =
    when (this) {
        is ProviderKeyTestResult.Success -> ApiKeyTestUiState(
            message = context.getString(R.string.api_key_test_success, provider),
            isSuccess = true,
        )
        is ProviderKeyTestResult.MissingKey -> ApiKeyTestUiState(
            message = context.getString(R.string.api_key_test_missing, provider),
            isSuccess = false,
        )
        is ProviderKeyTestResult.Failure -> ApiKeyTestUiState(
            message = context.getString(R.string.api_key_test_failed, provider, detail),
            isSuccess = false,
        )
    }
@Composable
internal fun AppSettingsScreen(
    container: VerballyContainer,
    savedSettings: AppSettings,
    onSettingsSaved: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var settings by remember(savedSettings) { mutableStateOf(savedSettings) }
    val context = LocalContext.current
    val selectThemeMode = { themeMode: AppThemeMode ->
        val updatedSettings = settings.copy(themeMode = themeMode)
        settings = updatedSettings
        container.settingsRepository.save(updatedSettings)
        onSettingsSaved(updatedSettings)
    }
    val selectInterfaceLanguage = { language: AppLanguage ->
        val updatedSettings = settings
            .withInterfaceLanguage(language)
            .withDefaultCleanupPromptLanguage(context.defaultPromptLanguageFor(language))
        settings = updatedSettings
        container.settingsRepository.save(updatedSettings)
        onSettingsSaved(updatedSettings)
        context.applyAppLanguage(language)
        context.findActivity()?.recreate()
        Unit
    }
    val selectHistoryRetentionMode = { mode: HistoryRetentionMode ->
        val updatedSettings = settings.copy(historyRetentionMode = mode)
        settings = updatedSettings
        container.settingsRepository.save(updatedSettings)
        if (mode == HistoryRetentionMode.NONE) {
            container.historyRepository.clear()
        } else {
            container.historyRepository.list()
        }
        onSettingsSaved(updatedSettings)
    }
    AppSettingsScreenContent(
        settings = settings,
        onThemeModeSelected = selectThemeMode,
        onInterfaceLanguageSelected = selectInterfaceLanguage,
        onHistoryRetentionModeSelected = selectHistoryRetentionMode,
        modifier = modifier,
    )
}

@Composable
fun AppSettingsScreenContent(
    settings: AppSettings,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    onInterfaceLanguageSelected: (AppLanguage) -> Unit = {},
    onHistoryRetentionModeSelected: (HistoryRetentionMode) -> Unit = {},
) {
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showInterfaceLanguageDialog by remember { mutableStateOf(false) }
    var showHistoryRetentionDialog by remember { mutableStateOf(false) }
    var pendingHistoryRetentionMode by remember { mutableStateOf<HistoryRetentionMode?>(null) }
    if (showAppearanceDialog) {
        SettingsChoiceDialog(
            title = { Text(stringResource(R.string.settings_appearance_mode)) },
            onDismiss = { showAppearanceDialog = false },
        ) {
            AppThemeMode.entries.forEach { mode ->
                ThemeModeRadioOption(
                    mode = mode,
                    selected = settings.themeMode == mode,
                    onSelected = {
                        showAppearanceDialog = false
                        onThemeModeSelected(mode)
                    },
                )
            }
        }
    }
    if (showInterfaceLanguageDialog) {
        SettingsChoiceDialog(
            title = { Text(stringResource(R.string.settings_interface_language)) },
            onDismiss = { showInterfaceLanguageDialog = false },
        ) {
            AppLanguage.entries.forEach { language ->
                InterfaceLanguageRadioOption(
                    language = language,
                    selected = settings.interfaceLanguage == language,
                    onSelected = {
                        showInterfaceLanguageDialog = false
                        onInterfaceLanguageSelected(language)
                    },
                )
            }
        }
    }
    if (showHistoryRetentionDialog) {
        SettingsChoiceDialog(
            title = { Text(stringResource(R.string.settings_history_retention)) },
            onDismiss = { showHistoryRetentionDialog = false },
        ) {
            HistoryRetentionMode.entries.forEach { mode ->
                HistoryRetentionModeRadioOption(
                    mode = mode,
                    selected = settings.historyRetentionMode == mode,
                    onSelected = {
                        showHistoryRetentionDialog = false
                        if (mode == settings.historyRetentionMode) return@HistoryRetentionModeRadioOption
                        if (mode.requiresConfirmation()) {
                            pendingHistoryRetentionMode = mode
                        } else {
                            onHistoryRetentionModeSelected(mode)
                        }
                    },
                )
            }
        }
    }
    pendingHistoryRetentionMode?.let { mode ->
        AlertDialog(
            onDismissRequest = { pendingHistoryRetentionMode = null },
            title = { Text(stringResource(R.string.settings_history_retention_confirm_title)) },
            text = { Text(mode.confirmationDescription()) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingHistoryRetentionMode = null
                        onHistoryRetentionModeSelected(mode)
                    },
                ) {
                    Text(stringResource(R.string.settings_history_retention_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingHistoryRetentionMode = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.settings_subtitle),
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsPickerRow(
                title = stringResource(R.string.settings_appearance_mode),
                value = settings.themeMode.localizedLabel(),
                contentDescription = stringResource(R.string.settings_open_appearance_picker),
                onClick = { showAppearanceDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsPickerRow(
                title = stringResource(R.string.settings_interface_language),
                value = settings.interfaceLanguage.localizedLabel(),
                contentDescription = stringResource(R.string.settings_open_interface_language_picker),
                onClick = { showInterfaceLanguageDialog = true },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsPickerRow(
                title = stringResource(R.string.settings_history_retention),
                value = settings.historyRetentionMode.localizedLabel(),
                contentDescription = stringResource(R.string.settings_open_history_retention_picker),
                onClick = { showHistoryRetentionDialog = true },
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun SettingsChoiceDialog(
    title: @Composable () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = title,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                content()
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun SettingsPickerRow(
    title: String,
    value: String,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_app_chevron_down_24),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ThemeModeRadioOption(
    mode: AppThemeMode,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    val label = mode.localizedLabel()
    val contentDescription = stringResource(R.string.settings_choose_appearance, label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SettingsChoiceRowHeight)
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton,
            )
            .semantics {
                this.contentDescription = contentDescription
            }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun InterfaceLanguageRadioOption(
    language: AppLanguage,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    val label = language.localizedLabel()
    val contentDescription = stringResource(R.string.settings_choose_interface_language, label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SettingsChoiceRowHeight)
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton,
            )
            .semantics {
                this.contentDescription = contentDescription
            }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HistoryRetentionModeRadioOption(
    mode: HistoryRetentionMode,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    val label = mode.localizedLabel()
    val contentDescription = stringResource(R.string.settings_choose_history_retention, label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SettingsChoiceRowHeight)
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton,
            )
            .semantics {
                this.contentDescription = contentDescription
            }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AppThemeMode.localizedLabel(): String = when (this) {
    AppThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    AppThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    AppThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
private fun HistoryRetentionMode.localizedLabel(): String = when (this) {
    HistoryRetentionMode.LATEST_100 -> stringResource(R.string.settings_history_retention_latest_100)
    HistoryRetentionMode.AUTO_DELETE_24_HOURS -> stringResource(R.string.settings_history_retention_auto_delete_24h)
    HistoryRetentionMode.NONE -> stringResource(R.string.settings_history_retention_none)
}

@Composable
private fun HistoryRetentionMode.confirmationDescription(): String = when (this) {
    HistoryRetentionMode.AUTO_DELETE_24_HOURS ->
        stringResource(R.string.settings_history_retention_confirm_auto_delete_description)
    HistoryRetentionMode.NONE ->
        stringResource(R.string.settings_history_retention_confirm_none_description)
    HistoryRetentionMode.LATEST_100 -> ""
}

private fun HistoryRetentionMode.requiresConfirmation(): Boolean =
    this == HistoryRetentionMode.AUTO_DELETE_24_HOURS || this == HistoryRetentionMode.NONE


@Composable
fun SettingsScreenContent(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    transcriptionTestState: ApiKeyTestUiState = ApiKeyTestUiState(),
    cleanupTestState: ApiKeyTestUiState = ApiKeyTestUiState(),
    onTestTranscriptionApiKey: () -> Unit = {},
    onTestCleanupApiKey: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.api_settings_title),
            subtitle = stringResource(R.string.api_settings_subtitle),
        )
        Text(
            text = stringResource(R.string.api_settings_order),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ApiSettingsBlock(
            title = stringResource(R.string.transcription_settings_title),
            subtitle = stringResource(R.string.transcription_settings_subtitle),
        ) {
            TranscriptionSettingsFields(
                settings = settings,
                onSettingsChange = onSettingsChange,
            )
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PrimaryActionHeight),
            ) {
                Text(stringResource(R.string.save_transcription_api_key))
            }
            ApiKeyTestAction(
                label = stringResource(R.string.test_transcription_api_key),
                state = transcriptionTestState,
                onClick = onTestTranscriptionApiKey,
            )
        }
        ApiSettingsBlock(
            title = stringResource(R.string.cleanup_settings_title),
            subtitle = stringResource(R.string.cleanup_settings_subtitle),
        ) {
            CleanupSettingsFields(
                settings = settings,
                onSettingsChange = onSettingsChange,
            )
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PrimaryActionHeight),
            ) {
                Text(stringResource(R.string.save_cleanup_settings))
            }
            ApiKeyTestAction(
                label = stringResource(R.string.test_cleanup_api_key),
                state = cleanupTestState,
                onClick = onTestCleanupApiKey,
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun ApiKeyTestAction(
    label: String,
    state: ApiKeyTestUiState,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !state.isTesting,
        modifier = Modifier
            .fillMaxWidth()
            .height(PrimaryActionHeight),
    ) {
        Text(
            if (state.isTesting) {
                stringResource(R.string.api_key_test_in_progress)
            } else {
                label
            },
        )
    }
    state.message?.let { message ->
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.isSuccess == true) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
    }
}

@Composable
private fun ApiSettingsBlock(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            content()
        }
    }
}

@Composable
private fun TranscriptionSettingsFields(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
) {
    DropdownField(
        label = stringResource(R.string.transcription_model_label),
        selectedValue = settings.transcriptionModelOptionLabel,
        options = ModelOptions.TranscriptionOptions.map { it.label },
        onSelected = { onSettingsChange(settings.withTranscriptionModelOption(it)) },
    )
    when (settings.transcriptionProvider) {
        TranscriptionProvider.OPENAI -> {
            SecretField(stringResource(R.string.api_key_label), settings.openAiApiKey) {
                onSettingsChange(settings.copy(openAiApiKey = it))
            }
        }
        TranscriptionProvider.SONIOX -> {
            SecretField(stringResource(R.string.api_key_label), settings.sonioxApiKey) {
                onSettingsChange(settings.copy(sonioxApiKey = it))
            }
        }
        TranscriptionProvider.GROQ -> {
            SecretField(stringResource(R.string.api_key_label), settings.groqApiKey) {
                onSettingsChange(settings.copy(groqApiKey = it))
            }
        }
    }
}

@Composable
private fun CleanupSettingsFields(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
) {
    val context = LocalContext.current
    DropdownField(
        label = stringResource(R.string.cleanup_model_label),
        selectedValue = settings.cleanupModelOptionLabel,
        options = ModelOptions.CleanupOptions.map { it.label },
        onSelected = { onSettingsChange(settings.withCleanupModelOption(it)) },
    )
    when (settings.cleanupProvider) {
        CleanupProvider.OPENAI -> {
            SecretField(stringResource(R.string.api_key_label), settings.openAiApiKey) {
                onSettingsChange(settings.copy(openAiApiKey = it))
            }
        }
        CleanupProvider.GEMINI -> {
            SecretField(stringResource(R.string.api_key_label), settings.geminiApiKey) {
                onSettingsChange(settings.copy(geminiApiKey = it))
            }
        }
    }
    CleanupPromptField(
        prompt = settings.cleanupPromptForDisplay(),
        onPromptChange = {
            onSettingsChange(
                settings.withCleanupPromptEdited(
                    prompt = it,
                    defaultPromptLanguage = context.defaultPromptLanguageFor(settings.interfaceLanguage),
                ),
            )
        },
        onRestoreDefault = {
            onSettingsChange(
                settings.withDefaultCleanupPromptRestored(
                    defaultPromptLanguage = context.defaultPromptLanguageFor(settings.interfaceLanguage),
                ),
            )
        },
    )
}

@Composable
private fun SecretField(label: String, value: String, onChange: (String) -> Unit) {
    LabeledTextField(
        label = label,
        value = value,
        onChange = onChange,
        visualTransformation = PasswordVisualTransformation(),
    )
}

@Composable
private fun CleanupPromptField(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onRestoreDefault: () -> Unit,
) {
    val promptMenuContentDescription = stringResource(R.string.basic_cleanup_prompt_menu)
    val promptInputContentDescription = stringResource(R.string.basic_cleanup_prompt_input)
    var showPromptMenu by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.basic_cleanup_prompt_label),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 48.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(
                    onClick = { showPromptMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .semantics {
                            contentDescription = promptMenuContentDescription
                        },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_more_vert_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                DropdownMenu(
                    expanded = showPromptMenu,
                    onDismissRequest = { showPromptMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.restore_default)) },
                        onClick = {
                            showPromptMenu = false
                            onRestoreDefault()
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .semantics { contentDescription = promptInputContentDescription },
            textStyle = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            visualTransformation = visualTransformation,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(FormFieldHeight),
        )
    }
}

@Composable
private fun DropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = selectedValue.takeIf { it in options } ?: options.firstOrNull().orEmpty()
    val displayParts = ModelOptionLabelParts.from(displayValue)
    val selectContentDescription = stringResource(R.string.dropdown_select_content_description, label)
    val expandContentDescription = stringResource(R.string.dropdown_expand_content_description, label)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ModelDropdownHeight)
                    .semantics {
                        contentDescription = selectContentDescription
                    },
                contentPadding = PaddingValues(start = 16.dp, end = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
                    ) {
                        displayParts.provider?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = displayParts.model,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_app_chevron_down_24),
                        contentDescription = expandContentDescription,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

private data class ModelOptionLabelParts(
    val provider: String?,
    val model: String,
) {
    companion object {
        fun from(label: String): ModelOptionLabelParts {
            val parts = label.split(": ", limit = 2)
            return if (parts.size == 2) {
                ModelOptionLabelParts(provider = parts[0], model = parts[1])
            } else {
                ModelOptionLabelParts(provider = null, model = label)
            }
        }
    }
}
