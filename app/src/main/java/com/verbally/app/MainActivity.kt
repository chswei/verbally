package com.verbally.app

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.permissions.PermissionAction
import com.verbally.app.permissions.PermissionGuidance
import com.verbally.app.permissions.PermissionSetupStep
import com.verbally.app.providers.CleanupPromptFactory
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.AppThemeMode
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.ModelOptions
import com.verbally.app.settings.TranscriptionProvider
import com.verbally.app.settings.cleanupModelOptionLabel
import com.verbally.app.settings.normalizedModelChoices
import com.verbally.app.settings.transcriptionModelOptionLabel
import com.verbally.app.settings.withCleanupModelOption
import com.verbally.app.settings.withTranscriptionModelOption
import com.verbally.app.snippets.SnippetEntry
import com.verbally.app.style.AppStyleProfile
import com.verbally.app.style.OutputStyle
import kotlinx.coroutines.launch

private val VerballyBrandBlue = Color(0xFF14233A)
private val VerballySoftBlue = Color(0xFFE6EDF6)
private val VerballyAccentTeal = Color(0xFF2F6F68)
private val VerballyAccentMint = Color(0xFFD6F1EA)
private val VerballyAccentLavender = Color(0xFFEDE7FF)
private val VerballyPageBackground = Color(0xFFF8FAFC)
private val VerballySurface = Color(0xFFFFFFFF)
private val VerballyOutline = Color(0xFFD1D9E4)
private val ScreenHorizontalPadding = 24.dp
private val ScreenVerticalPadding = 12.dp
private val FormFieldHeight = 56.dp
private val ModelDropdownHeight = 64.dp
private val PrimaryActionHeight = 52.dp

private val VerballyColorScheme = lightColorScheme(
    primary = VerballyBrandBlue,
    onPrimary = Color.White,
    primaryContainer = VerballySoftBlue,
    onPrimaryContainer = VerballyBrandBlue,
    secondary = VerballyAccentTeal,
    onSecondary = Color.White,
    secondaryContainer = VerballyAccentMint,
    onSecondaryContainer = Color(0xFF123532),
    tertiary = Color(0xFF6A5CA8),
    onTertiary = Color.White,
    tertiaryContainer = VerballyAccentLavender,
    onTertiaryContainer = Color(0xFF2B2456),
    background = VerballyPageBackground,
    onBackground = Color(0xFF171C22),
    surface = VerballySurface,
    onSurface = Color(0xFF171C22),
    surfaceVariant = Color(0xFFE8EDF4),
    onSurfaceVariant = Color(0xFF465464),
    outline = VerballyOutline,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val VerballyDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB9C7E8),
    onPrimary = Color(0xFF243049),
    primaryContainer = Color(0xFF33415D),
    onPrimaryContainer = Color(0xFFDCE5FF),
    secondary = Color(0xFF9BCFC6),
    onSecondary = Color(0xFF063A35),
    secondaryContainer = Color(0xFF234E48),
    onSecondaryContainer = Color(0xFFB7ECE3),
    tertiary = Color(0xFFCFC2FF),
    onTertiary = Color(0xFF372D65),
    tertiaryContainer = Color(0xFF564A88),
    onTertiaryContainer = Color(0xFFE8DEFF),
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE0E2E8),
    surface = Color(0xFF1B1F24),
    onSurface = Color(0xFFE0E2E8),
    surfaceVariant = Color(0xFF42474F),
    onSurfaceVariant = Color(0xFFC3C7D0),
    outline = Color(0xFF8C919A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val VerballyTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
)

private val VerballyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as VerballyApplication).container
        setContent {
            var appSettings by remember {
                mutableStateOf(container.settingsRepository.load().normalizedModelChoices())
            }
            VerballyTheme(themeMode = appSettings.themeMode) {
                VerballyApp(
                    container = container,
                    appSettings = appSettings,
                    onSettingsSaved = { appSettings = it.normalizedModelChoices() },
                )
            }
        }
    }
}

@Composable
fun VerballyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val colorScheme = if (useDarkTheme) VerballyDarkColorScheme else VerballyColorScheme
    VerballySystemBarEffect(
        useDarkTheme = useDarkTheme,
        statusBarColor = colorScheme.background,
        navigationBarColor = colorScheme.surface,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VerballyTypography,
        shapes = VerballyShapes,
        content = content,
    )
}

@Composable
private fun VerballySystemBarEffect(
    useDarkTheme: Boolean,
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    val view = LocalView.current
    SideEffect {
        if (view.isInEditMode) return@SideEffect
        val activity = view.context.findActivity() as? ComponentActivity ?: return@SideEffect
        val statusBarArgb = statusBarColor.toArgb()
        val navigationBarArgb = navigationBarColor.toArgb()
        activity.enableEdgeToEdge(
            statusBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(statusBarArgb)
            } else {
                SystemBarStyle.light(statusBarArgb, statusBarArgb)
            },
            navigationBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(navigationBarArgb)
            } else {
                SystemBarStyle.light(navigationBarArgb, navigationBarArgb)
            },
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal enum class PermissionScreenOpenReason {
    REQUIRED_SETUP,
    SUPPORT,
}

internal fun shouldDismissPermissionScreenOnPermissionRefresh(
    openReason: PermissionScreenOpenReason,
    permissionsReady: Boolean,
): Boolean = permissionsReady && openReason == PermissionScreenOpenReason.REQUIRED_SETUP

@Composable
fun VerballyApp(
    container: VerballyContainer,
    appSettings: AppSettings,
    onSettingsSaved: (AppSettings) -> Unit,
) {
    val context = LocalContext.current
    var selectedDestination by remember { mutableStateOf(AppDestination.HOME) }
    var showingAppSettings by remember { mutableStateOf(false) }
    var permissionsReady by remember { mutableStateOf(hasRequiredPermissions(context)) }
    var showingPermissions by remember { mutableStateOf(!permissionsReady) }
    var permissionScreenOpenReason by remember {
        mutableStateOf(
            if (permissionsReady) PermissionScreenOpenReason.SUPPORT else PermissionScreenOpenReason.REQUIRED_SETUP,
        )
    }

    fun refreshPermissions(openReason: PermissionScreenOpenReason = permissionScreenOpenReason) {
        val currentPermissionsReady = hasRequiredPermissions(context)
        permissionsReady = currentPermissionsReady
        if (
            shouldDismissPermissionScreenOnPermissionRefresh(
                openReason = openReason,
                permissionsReady = currentPermissionsReady,
            )
        ) {
            showingPermissions = false
        }
    }

    if (showingPermissions) {
        PermissionScreen(
            onPermissionsChanged = { refreshPermissions() },
            onComplete = {
                refreshPermissions(PermissionScreenOpenReason.REQUIRED_SETUP)
                showingPermissions = false
            },
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    VerballyAppScaffold(
        permissionsReady = permissionsReady,
        selectedDestination = selectedDestination,
        showingSettings = showingAppSettings,
        onDestinationSelected = {
            refreshPermissions()
            showingAppSettings = false
            selectedDestination = it
        },
        onOpenSettings = {
            showingAppSettings = true
        },
        onOpenPermissions = {
            permissionScreenOpenReason = if (permissionsReady) {
                PermissionScreenOpenReason.SUPPORT
            } else {
                PermissionScreenOpenReason.REQUIRED_SETUP
            }
            showingPermissions = true
        },
        homeContent = {
            SettingsScreen(
                container = container,
                savedSettings = appSettings,
                onSettingsSaved = onSettingsSaved,
                modifier = Modifier.fillMaxSize(),
            )
        },
        dictionaryContent = {
            DictionaryScreen(
                container = container,
                modifier = Modifier.fillMaxSize(),
            )
        },
        snippetsContent = {
            SnippetsScreen(
                container = container,
                modifier = Modifier.fillMaxSize(),
            )
        },
        historyContent = {
            HistoryScreen(
                container = container,
                modifier = Modifier.fillMaxSize(),
            )
        },
        styleContent = {
            StyleProfilesScreen(
                container = container,
                modifier = Modifier.fillMaxSize(),
            )
        },
        settingsContent = {
            AppSettingsScreen(
                container = container,
                savedSettings = appSettings,
                onSettingsSaved = onSettingsSaved,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}

enum class AppDestination(val label: String, @param:DrawableRes val iconRes: Int) {
    HOME("首頁", R.drawable.ic_app_home_24),
    DICTIONARY("字典", R.drawable.ic_app_dictionary_24),
    SNIPPETS("片段", R.drawable.ic_app_snippets_24),
    STYLE("語氣", R.drawable.ic_app_style_24),
    HISTORY("歷史", R.drawable.ic_app_history_24),
}

@Composable
fun VerballyAppScaffold(
    permissionsReady: Boolean,
    showingSettings: Boolean,
    selectedDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPermissions: () -> Unit,
    homeContent: @Composable () -> Unit,
    dictionaryContent: @Composable () -> Unit,
    snippetsContent: @Composable () -> Unit,
    historyContent: @Composable () -> Unit,
    styleContent: @Composable () -> Unit,
    settingsContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            VerballyDrawerContent(
                onOpenSettings = {
                    scope.launch {
                        drawerState.close()
                        onOpenSettings()
                    }
                },
                onOpenPermissions = {
                    scope.launch {
                        drawerState.close()
                        onOpenPermissions()
                    }
                },
            )
        },
    ) {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                VerballyTopBar(
                    onOpenMenu = { scope.launch { drawerState.open() } },
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    AppDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = !showingSettings && selectedDestination == destination,
                            onClick = { onDestinationSelected(destination) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            icon = {
                                Icon(
                                    painter = painterResource(destination.iconRes),
                                    contentDescription = null,
                                )
                            },
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
                    if (showingSettings) {
                        settingsContent()
                    } else {
                        when (selectedDestination) {
                            AppDestination.HOME -> homeContent()
                            AppDestination.DICTIONARY -> dictionaryContent()
                            AppDestination.SNIPPETS -> snippetsContent()
                            AppDestination.HISTORY -> historyContent()
                            AppDestination.STYLE -> styleContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerballyTopBar(
    onOpenMenu: () -> Unit,
) {
    Surface(
        modifier = Modifier.statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_app_menu_24),
                    contentDescription = "開啟選單",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "Verbally",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun VerballyDrawerContent(
    onOpenSettings: () -> Unit,
    onOpenPermissions: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.width(292.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            Text(
                text = "管理與支援",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "設定、權限與疑難排解",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))
            DrawerSectionLabel(text = "應用程式")
            NavigationDrawerItem(
                modifier = Modifier.semantics { contentDescription = "設定選單項目" },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_settings_24),
                        contentDescription = null,
                    )
                },
                label = {
                    DrawerItemLabel(
                        title = "設定",
                        subtitle = "外觀與 App 偏好",
                    )
                },
                selected = false,
                onClick = onOpenSettings,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            DrawerSectionLabel(text = "支援")
            NavigationDrawerItem(
                modifier = Modifier.semantics { contentDescription = "權限與疑難排解選單項目" },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_support_24),
                        contentDescription = null,
                    )
                },
                label = {
                    DrawerItemLabel(
                        title = "權限與疑難排解",
                        subtitle = "錄音、浮動視窗與輔助使用",
                    )
                },
                selected = false,
                onClick = onOpenPermissions,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                ),
            )
        }
    }
}

@Composable
private fun DrawerSectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun DrawerItemLabel(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PermissionScreen(
    onPermissionsChanged: () -> Unit,
    onComplete: () -> Unit = onPermissionsChanged,
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
                onComplete()
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
            PlainPageTopBar("權限設定")
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
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        if (step != PermissionSetupStep.COMPLETE) {
            PermissionStepCard(details = details)
        } else {
            Text(
                text = details.title,
                style = MaterialTheme.typography.headlineSmall,
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
            modifier = Modifier
                .fillMaxWidth()
                .height(PrimaryActionHeight),
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
            modifier = Modifier.padding(18.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PrimaryActionHeight),
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                        text = details.progress,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = details.title,
                style = MaterialTheme.typography.headlineSmall,
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
    savedSettings: AppSettings,
    onSettingsSaved: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var settings by remember(savedSettings) { mutableStateOf(savedSettings.normalizedModelChoices()) }
    val context = LocalContext.current
    val saveSettings = {
        val normalizedSettings = settings.normalizedModelChoices()
        container.settingsRepository.save(normalizedSettings)
        onSettingsSaved(normalizedSettings)
        Toast.makeText(context, "設定已儲存", Toast.LENGTH_SHORT).show()
    }

    SettingsScreenContent(
        settings = settings,
        onSettingsChange = { settings = it },
        onSave = saveSettings,
        modifier = modifier,
    )
}

@Composable
private fun AppSettingsScreen(
    container: VerballyContainer,
    savedSettings: AppSettings,
    onSettingsSaved: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var settings by remember(savedSettings) { mutableStateOf(savedSettings.normalizedModelChoices()) }
    val selectThemeMode = { themeMode: AppThemeMode ->
        val updatedSettings = settings.copy(themeMode = themeMode).normalizedModelChoices()
        settings = updatedSettings
        container.settingsRepository.save(updatedSettings)
        onSettingsSaved(updatedSettings)
    }

    AppSettingsScreenContent(
        settings = settings,
        onThemeModeSelected = selectThemeMode,
        modifier = modifier,
    )
}

@Composable
fun AppSettingsScreenContent(
    settings: AppSettings,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = "設定",
            subtitle = "調整 Verbally 的外觀顯示模式。",
        )
        SectionLabel("外觀模式")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppThemeMode.entries.forEach { mode ->
                ThemeModeRadioOption(
                    mode = mode,
                    selected = settings.themeMode == mode,
                    onSelected = { onThemeModeSelected(mode) },
                )
            }
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun ThemeModeRadioOption(
    mode: AppThemeMode,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(FormFieldHeight)
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton,
            )
            .semantics { contentDescription = "選擇 ${mode.label}外觀" }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Text(
            text = mode.label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun SettingsScreenContent(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = "API 設定",
            subtitle = "先完成語音轉錄，再完成文字處理；兩個都儲存後就能用浮動按鈕聽寫。",
        )
        Text(
            text = "設定順序：語音轉錄 → 文字處理 → 開始聽寫",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ApiSettingsBlock(
            title = "語音轉錄",
            subtitle = "選擇語音辨識模型，貼上 OpenAI API Key 後儲存。",
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
                Text("儲存語音轉錄 API Key")
            }
        }
        ApiSettingsBlock(
            title = "文字處理",
            subtitle = "選擇整理文字的模型；切換服務時只會顯示對應的 API Key。",
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
                Text("儲存文字處理設定")
            }
        }
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
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(title = "語音轉錄", onBack = onBack)
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
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(title = "文字處理", onBack = onBack)
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
            Text("儲存設定")
        }
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
        label = "語音轉錄模型",
        selectedValue = settings.transcriptionModelOptionLabel,
        options = ModelOptions.TranscriptionOptions.map { it.label },
        onSelected = { onSettingsChange(settings.withTranscriptionModelOption(it)) },
    )
    when (settings.transcriptionProvider) {
        TranscriptionProvider.OPENAI -> {
            SecretField("API Key", settings.openAiApiKey) {
                onSettingsChange(settings.copy(openAiApiKey = it))
            }
        }
        TranscriptionProvider.SONIOX -> {
            SecretField("API Key", settings.sonioxApiKey) {
                onSettingsChange(settings.copy(sonioxApiKey = it))
            }
        }
        TranscriptionProvider.GROQ -> {
            SecretField("API Key", settings.groqApiKey) {
                onSettingsChange(settings.copy(groqApiKey = it))
            }
        }
        TranscriptionProvider.DEEPGRAM -> {
            SecretField("API Key", settings.deepgramApiKey) {
                onSettingsChange(settings.copy(deepgramApiKey = it))
            }
        }
    }
}

@Composable
private fun CleanupSettingsFields(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
) {
    DropdownField(
        label = "文字處理模型",
        selectedValue = settings.cleanupModelOptionLabel,
        options = ModelOptions.CleanupOptions.map { it.label },
        onSelected = { onSettingsChange(settings.withCleanupModelOption(it)) },
    )
    when (settings.cleanupProvider) {
        CleanupProvider.OPENAI -> {
            SecretField("API Key", settings.openAiApiKey) {
                onSettingsChange(settings.copy(openAiApiKey = it))
            }
        }
        CleanupProvider.GEMINI -> {
            SecretField("API Key", settings.geminiApiKey) {
                onSettingsChange(settings.copy(geminiApiKey = it))
            }
        }
    }
    CleanupPromptField(
        prompt = settings.cleanupPrompt,
        onPromptChange = { onSettingsChange(settings.copy(cleanupPrompt = it)) },
        onRestoreDefault = {
            onSettingsChange(settings.copy(cleanupPrompt = CleanupPromptFactory.defaultCleanupPrompt))
        },
    )
}

@Composable
private fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (onBack != null) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        "←",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Column(
                modifier = Modifier.padding(top = if (onBack != null) 2.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "基本文字處理提示詞",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRestoreDefault) {
                Text("還原預設")
            }
        }
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .semantics { contentDescription = "基本文字處理提示詞輸入" },
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
                    .semantics { contentDescription = "選擇 $label" },
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
                        contentDescription = "$label 展開箭頭",
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

@Composable
private fun SearchField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String = "搜尋",
    contentDescription: String? = null,
) {
    val fieldModifier = Modifier
        .fillMaxWidth()
        .height(FormFieldHeight)
        .let { modifier ->
            if (contentDescription == null) {
                modifier
            } else {
                modifier.semantics { this.contentDescription = contentDescription }
            }
        }

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = fieldModifier,
    )
}

@Composable
private fun DictionaryScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.dictionaryRepository.list()) }
    val context = LocalContext.current

    fun refresh(nextQuery: String = query) {
        entries = container.dictionaryRepository.search(nextQuery)
    }

    DictionaryScreenContent(
        query = query,
        entries = entries,
        onQueryChange = {
            query = it
            refresh(it)
        },
        onSave = { entry ->
            container.dictionaryRepository.save(entry)
            refresh()
            Toast.makeText(context, "字典已儲存", Toast.LENGTH_SHORT).show()
        },
        onDelete = { entry ->
            container.dictionaryRepository.delete(entry.id)
            refresh()
            Toast.makeText(context, "字典詞彙已刪除", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier,
    )
}

@Composable
fun DictionaryScreenContent(
    query: String,
    entries: List<DictionaryEntry>,
    onQueryChange: (String) -> Unit,
    onSave: (DictionaryEntry) -> Unit,
    onDelete: (DictionaryEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingEntry by remember { mutableStateOf<DictionaryEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    if (showEditor) {
        DictionaryEntryDialog(
            entry = editingEntry,
            onDismiss = {
                showEditor = false
                editingEntry = null
            },
            onSave = { entry ->
                onSave(entry)
                showEditor = false
                editingEntry = null
            },
        )
    }

    Box(
        modifier = modifier.padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ScreenHeader(
                title = "字典",
                subtitle = "保存常用詞、專有名詞與偏好的寫法，讓之後整理文字時更好找。",
            )
            SearchField(
                value = query,
                onChange = onQueryChange,
                placeholder = "搜尋字典",
                contentDescription = "搜尋字典輸入",
            )
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateBlock(
                        title = if (query.isBlank()) "尚未建立字典詞彙" else "找不到符合的字典詞彙",
                        description = if (query.isBlank()) {
                            "新增常用詞或專有名詞後，之後可以在這裡快速查找。"
                        } else {
                            "換個關鍵字，或新增這個詞彙讓之後整理文字時使用。"
                        },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(entries, key = { it.id }) { entry ->
                        DictionaryEntryCard(
                            entry = entry,
                            onEdit = {
                                editingEntry = entry
                                showEditor = true
                            },
                            onDelete = { onDelete(entry) },
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                editingEntry = null
                showEditor = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
                .semantics { contentDescription = "新增字典詞彙" },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun DictionaryEntryCard(
    entry: DictionaryEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = entry.term,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.semantics { contentDescription = "編輯 ${entry.term}" },
                ) {
                    Text("編輯")
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = "刪除 ${entry.term}" },
                ) {
                    Text("刪除")
                }
            }
        }
    }
}

@Composable
private fun DictionaryEntryDialog(
    entry: DictionaryEntry?,
    onDismiss: () -> Unit,
    onSave: (DictionaryEntry) -> Unit,
) {
    var term by remember(entry?.id) { mutableStateOf(entry?.term.orEmpty()) }
    var note by remember(entry?.id) { mutableStateOf(entry?.note.orEmpty()) }
    val title = if (entry == null) "新增字典詞彙" else "編輯字典詞彙"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = term,
                    onValueChange = { term = it },
                    label = { Text("詞彙") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "字典詞彙輸入" },
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("備註") },
                    placeholder = { Text("例如：品牌名、姓名、偏好大小寫") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "字典備註輸入" },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        DictionaryEntry(
                            id = entry?.id ?: System.currentTimeMillis(),
                            term = term,
                            note = note,
                        ),
                    )
                },
                enabled = term.trim().isNotEmpty(),
            ) {
                Text("儲存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun SnippetsScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.snippetRepository.list()) }
    val context = LocalContext.current

    fun refresh(nextQuery: String = query) {
        entries = container.snippetRepository.search(nextQuery)
    }

    SnippetsScreenContent(
        query = query,
        entries = entries,
        onQueryChange = {
            query = it
            refresh(it)
        },
        onSave = { entry ->
            container.snippetRepository.save(entry)
            refresh()
            Toast.makeText(context, "片段已儲存", Toast.LENGTH_SHORT).show()
        },
        onDelete = { entry ->
            container.snippetRepository.delete(entry.id)
            refresh()
            Toast.makeText(context, "片段已刪除", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier,
    )
}

@Composable
fun SnippetsScreenContent(
    query: String,
    entries: List<SnippetEntry>,
    onQueryChange: (String) -> Unit,
    onSave: (SnippetEntry) -> Unit,
    onDelete: (SnippetEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingEntry by remember { mutableStateOf<SnippetEntry?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    if (showEditor) {
        SnippetEntryDialog(
            entry = editingEntry,
            onDismiss = {
                showEditor = false
                editingEntry = null
            },
            onSave = { entry ->
                onSave(entry)
                showEditor = false
                editingEntry = null
            },
        )
    }

    Box(
        modifier = modifier.padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ScreenHeader(
                title = "片段",
                subtitle = "保存觸發詞與展開文字；聽寫時說出觸發詞，就會插入完整內容。",
            )
            SearchField(
                value = query,
                onChange = onQueryChange,
                placeholder = "搜尋片段",
                contentDescription = "搜尋片段輸入",
            )
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateBlock(
                        title = if (query.isBlank()) "尚未建立常用片段" else "找不到符合的常用片段",
                        description = if (query.isBlank()) {
                            "新增像「我的地址」或「放射科報告模板」這類觸發詞，之後聽寫會展開成完整文字。"
                        } else {
                            "換個關鍵字，或新增這個觸發詞讓之後聽寫時展開。"
                        },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    items(entries, key = { it.id }) { entry ->
                        SnippetEntryCard(
                            entry = entry,
                            onEdit = {
                                editingEntry = entry
                                showEditor = true
                            },
                            onDelete = { onDelete(entry) },
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                editingEntry = null
                showEditor = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
                .semantics { contentDescription = "新增常用片段" },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun SnippetEntryCard(
    entry: SnippetEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = entry.trigger,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = entry.expansion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.semantics { contentDescription = "編輯 ${entry.trigger}" },
                ) {
                    Text("編輯")
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics { contentDescription = "刪除 ${entry.trigger}" },
                ) {
                    Text("刪除")
                }
            }
        }
    }
}

@Composable
private fun SnippetEntryDialog(
    entry: SnippetEntry?,
    onDismiss: () -> Unit,
    onSave: (SnippetEntry) -> Unit,
) {
    var trigger by remember(entry?.id) { mutableStateOf(entry?.trigger.orEmpty()) }
    var expansion by remember(entry?.id) { mutableStateOf(entry?.expansion.orEmpty()) }
    val title = if (entry == null) "新增常用片段" else "編輯常用片段"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text("觸發詞") },
                    placeholder = { Text("例如：我的地址") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "片段觸發詞輸入" },
                )
                OutlinedTextField(
                    value = expansion,
                    onValueChange = { expansion = it },
                    label = { Text("展開內容") },
                    placeholder = { Text("輸入要插入的完整文字或模板") },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "片段展開內容輸入" },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        SnippetEntry(
                            id = entry?.id ?: System.currentTimeMillis(),
                            trigger = trigger,
                            expansion = expansion,
                        ),
                    )
                },
                enabled = trigger.trim().isNotEmpty() && expansion.trim().isNotEmpty(),
            ) {
                Text("儲存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun StyleProfilesScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var profiles by remember { mutableStateOf(container.styleProfileRepository.list()) }
    val context = LocalContext.current

    StyleProfilesScreenContent(
        profiles = profiles,
        onProfileChange = { profile ->
            container.styleProfileRepository.save(profile)
            profiles = container.styleProfileRepository.list()
            Toast.makeText(context, "語氣已更新", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier,
    )
}

@Composable
fun StyleProfilesScreenContent(
    profiles: List<AppStyleProfile>,
    onProfileChange: (AppStyleProfile) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = "語氣風格",
            subtitle = "不同類型的 App 可以使用不同語氣；基本文字處理會先整理內容，再套用這裡的風格。",
        )
        profiles.forEach { profile ->
            StyleProfileRow(
                profile = profile,
                onProfileChange = onProfileChange,
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun StyleProfileRow(
    profile: AppStyleProfile,
    onProfileChange: (AppStyleProfile) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = profile.category.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = styleProfileDescription(profile),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutputStyle.entries.forEach { outputStyle ->
                    val selected = profile.style == outputStyle
                    val buttonModifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .semantics { contentDescription = "${profile.category.label} ${outputStyle.label}" }
                    if (selected) {
                        Button(
                            onClick = { onProfileChange(profile.copy(style = outputStyle)) },
                            modifier = buttonModifier,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text(outputStyle.label)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onProfileChange(profile.copy(style = outputStyle)) },
                            modifier = buttonModifier,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text(outputStyle.label)
                        }
                    }
                }
            }
        }
    }
}

private fun styleProfileDescription(profile: AppStyleProfile): String =
    when (profile.category.label) {
        "聊天" -> "LINE、Messenger、WhatsApp 等聊天 App 預設比較自然口語。"
        "工作" -> "Email、Slack、Teams、文件與筆記 App 預設比較完整正式。"
        else -> "無法分類或尚未支援的 App 會使用這組預設。"
    }

@Composable
private fun PlaceholderDataScreen(
    title: String,
    subtitle: String,
    query: String,
    onQueryChange: (String) -> Unit,
    searchPlaceholder: String,
    emptyTitle: String,
    emptyDescription: String,
    addContentDescription: String,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            ScreenHeader(title = title, subtitle = subtitle)
            SearchField(value = query, onChange = onQueryChange, placeholder = searchPlaceholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateBlock(
                    title = emptyTitle,
                    description = emptyDescription,
                )
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
                .semantics { contentDescription = addContentDescription },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun EmptyStateBlock(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = description,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HistoryScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.historyRepository.list()) }
    val context = LocalContext.current

    HistoryScreenContent(
        query = query,
        entries = entries,
        onQueryChange = {
            query = it
            entries = container.historyRepository.search(it)
        },
        onClearHistory = {
            container.historyRepository.clear()
            entries = container.historyRepository.search(query)
            Toast.makeText(context, "歷史已清空", Toast.LENGTH_SHORT).show()
        },
        onCopy = { entry ->
            copyText(context, entry.cleanedText)
            Toast.makeText(context, "已複製", Toast.LENGTH_SHORT).show()
        },
        onDelete = { entry ->
            container.historyRepository.delete(entry.id)
            entries = container.historyRepository.search(query)
        },
        modifier = modifier,
    )
}

@Composable
fun HistoryScreenContent(
    query: String,
    entries: List<DictationHistoryEntry>,
    onQueryChange: (String) -> Unit,
    onClearHistory: () -> Unit,
    onCopy: (DictationHistoryEntry) -> Unit,
    onDelete: (DictationHistoryEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showHistoryMenu by remember { mutableStateOf(false) }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("確定刪除歷史？") },
            text = { Text("這會刪除所有保存在這台裝置上的轉錄歷史，刪除後無法復原。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        onClearHistory()
                    },
                ) {
                    Text("確定刪除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("取消")
                }
            },
        )
    }

    Column(
        modifier = modifier.padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HistoryScreenHeader(
            showOverflow = entries.isNotEmpty(),
            overflowExpanded = showHistoryMenu,
            onOverflowClick = { showHistoryMenu = true },
            onOverflowDismiss = { showHistoryMenu = false },
            onClearHistoryClick = {
                showHistoryMenu = false
                showClearConfirmation = true
            },
        )
        SearchField(value = query, onChange = onQueryChange, placeholder = "搜尋歷史")
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        EmptyStateBlock(
                            title = "尚無轉錄歷史",
                            description = "完成聽寫後，整理好的文字會保存在這裡，最多保留最近 100 筆。",
                        )
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        onCopy = { onCopy(entry) },
                        onDelete = { onDelete(entry) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryScreenHeader(
    showOverflow: Boolean,
    overflowExpanded: Boolean,
    onOverflowClick: () -> Unit,
    onOverflowDismiss: () -> Unit,
    onClearHistoryClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        ScreenHeader(
            title = "歷史",
            subtitle = "只保留最近 100 筆轉錄紀錄",
        )
        if (showOverflow) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-6).dp),
            ) {
                IconButton(
                    onClick = onOverflowClick,
                    modifier = Modifier.semantics { contentDescription = "歷史選單" },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_more_vert_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = onOverflowDismiss,
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "清空歷史",
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = onClearHistoryClick,
                    )
                }
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
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) { Text("複製") }
                TextButton(onClick = onDelete) { Text("刪除") }
            }
        }
    }
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
