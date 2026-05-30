package com.verbally.app

import android.Manifest
import android.app.Activity
import android.app.LocaleManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.LocaleList
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
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
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.verbally.app.history.DictationHistoryEntry
import com.verbally.app.dictionary.DictionaryEntry
import com.verbally.app.permissions.PermissionAction
import com.verbally.app.permissions.PermissionGuidance
import com.verbally.app.permissions.PermissionSetupStep
import com.verbally.app.settings.AppLanguage
import com.verbally.app.settings.AppSettings
import com.verbally.app.settings.AppThemeMode
import com.verbally.app.settings.CleanupProvider
import com.verbally.app.settings.ModelOptions
import com.verbally.app.settings.cleanupPromptForDisplay
import com.verbally.app.settings.TranscriptionProvider
import com.verbally.app.settings.cleanupModelOptionLabel
import com.verbally.app.settings.normalizedModelChoices
import com.verbally.app.settings.withCleanupPromptEdited
import com.verbally.app.settings.transcriptionModelOptionLabel
import com.verbally.app.settings.withCleanupModelOption
import com.verbally.app.settings.withDefaultCleanupPromptLanguage
import com.verbally.app.settings.withDefaultCleanupPromptRestored
import com.verbally.app.settings.withInterfaceLanguage
import com.verbally.app.settings.withTranscriptionModelOption
import com.verbally.app.snippets.SnippetEntry
import com.verbally.app.style.AppCategory
import com.verbally.app.style.AppStyleProfile
import com.verbally.app.style.AppStyleRule
import com.verbally.app.style.OutputStyle
import com.verbally.app.style.StyleRuleDefaults
import com.verbally.app.style.normalizedStyleRuleLanguage
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
private val SettingsChoiceRowHeight = 48.dp

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
        val savedSettings = container.settingsRepository.load().normalizedModelChoices()
        applyAppLanguage(savedSettings.interfaceLanguage)
        val loadedSettings = savedSettings.withDefaultCleanupPromptLanguage(
            defaultPromptLanguageFor(savedSettings.interfaceLanguage),
        )
        setContent {
            var appSettings by remember { mutableStateOf(loadedSettings) }
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

private fun Context.applyAppLanguage(language: AppLanguage) {
    val localeManager = getSystemService(LocaleManager::class.java)
    localeManager.applicationLocales = if (language == AppLanguage.SYSTEM) {
        LocaleList.getEmptyLocaleList()
    } else {
        LocaleList.forLanguageTags(language.languageTag)
    }
}

private fun Context.defaultPromptLanguageFor(language: AppLanguage): AppLanguage =
    AppLanguage.defaultPromptLanguageFor(
        selectedInterfaceLanguage = language,
        systemLanguageTag = getSystemService(LocaleManager::class.java).systemLocales[0]?.toLanguageTag(),
    )

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
    var styleRuleEditorActive by remember { mutableStateOf(false) }
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
            styleRuleEditorActive = false
            selectedDestination = it
        },
        onOpenSettings = {
            styleRuleEditorActive = false
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
                onEditorActiveChange = { styleRuleEditorActive = it },
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
        showAppChrome = !styleRuleEditorActive,
    )
}

enum class AppDestination(@param:StringRes val labelRes: Int, @param:DrawableRes val iconRes: Int) {
    HOME(R.string.nav_home, R.drawable.ic_app_home_24),
    DICTIONARY(R.string.nav_dictionary, R.drawable.ic_app_dictionary_24),
    SNIPPETS(R.string.nav_snippets, R.drawable.ic_app_snippets_24),
    STYLE(R.string.nav_style, R.drawable.ic_app_style_24),
    HISTORY(R.string.nav_history, R.drawable.ic_app_history_24),
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
    showAppChrome: Boolean = true,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showAppChrome,
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
                if (showAppChrome) {
                    VerballyTopBar(
                        onOpenMenu = { scope.launch { drawerState.open() } },
                    )
                }
            },
            bottomBar = {
                if (showAppChrome) {
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
                                label = { Text(stringResource(destination.labelRes)) },
                            )
                        }
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
                    contentDescription = stringResource(R.string.open_menu_content_description),
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
    val menuSettingsContentDescription = stringResource(R.string.drawer_settings_item_content_description)
    val menuPermissionsContentDescription = stringResource(R.string.drawer_permissions_item_content_description)

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
                text = stringResource(R.string.drawer_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.drawer_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(14.dp))
            DrawerSectionLabel(text = stringResource(R.string.drawer_section_app))
            NavigationDrawerItem(
                modifier = Modifier.semantics {
                    contentDescription = menuSettingsContentDescription
                },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_settings_24),
                        contentDescription = null,
                    )
                },
                label = {
                    DrawerItemLabel(
                        title = stringResource(R.string.settings_title),
                        subtitle = stringResource(R.string.drawer_settings_subtitle),
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
            DrawerSectionLabel(text = stringResource(R.string.drawer_section_support))
            NavigationDrawerItem(
                modifier = Modifier.semantics {
                    contentDescription = menuPermissionsContentDescription
                },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_support_24),
                        contentDescription = null,
                    )
                },
                label = {
                    DrawerItemLabel(
                        title = stringResource(R.string.drawer_permissions_title),
                        subtitle = stringResource(R.string.drawer_permissions_subtitle),
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
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
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
            OutlinedButton(
                onClick = onOpenAppDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PrimaryActionHeight),
            ) {
                Text(stringResource(R.string.open_app_info))
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
            Button(onClick = onOpenPermissions) {
                Text(stringResource(R.string.permission_banner_button))
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
    val settingsSavedMessage = stringResource(R.string.settings_saved)
    val saveSettings = {
        val normalizedSettings = settings.normalizedModelChoices()
        container.settingsRepository.save(normalizedSettings)
        onSettingsSaved(normalizedSettings)
        Toast.makeText(context, settingsSavedMessage, Toast.LENGTH_SHORT).show()
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
    val context = LocalContext.current
    val selectThemeMode = { themeMode: AppThemeMode ->
        val updatedSettings = settings.copy(themeMode = themeMode).normalizedModelChoices()
        settings = updatedSettings
        container.settingsRepository.save(updatedSettings)
        onSettingsSaved(updatedSettings)
    }
    val selectInterfaceLanguage = { language: AppLanguage ->
        val updatedSettings = settings
            .withInterfaceLanguage(language)
            .withDefaultCleanupPromptLanguage(context.defaultPromptLanguageFor(language))
            .normalizedModelChoices()
        settings = updatedSettings
        container.settingsRepository.save(updatedSettings)
        onSettingsSaved(updatedSettings)
        context.applyAppLanguage(language)
        context.findActivity()?.recreate()
        Unit
    }

    AppSettingsScreenContent(
        settings = settings,
        onThemeModeSelected = selectThemeMode,
        onInterfaceLanguageSelected = selectInterfaceLanguage,
        modifier = modifier,
    )
}

@Composable
fun AppSettingsScreenContent(
    settings: AppSettings,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    onInterfaceLanguageSelected: (AppLanguage) -> Unit = {},
) {
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showInterfaceLanguageDialog by remember { mutableStateOf(false) }

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
private fun AppThemeMode.localizedLabel(): String = when (this) {
    AppThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    AppThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    AppThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
private fun AppLanguage.localizedLabel(): String = when (this) {
    AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
    else -> label
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
        ScreenHeader(title = stringResource(R.string.transcription_settings_title), onBack = onBack)
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
            Text(stringResource(R.string.save_settings))
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
        ScreenHeader(title = stringResource(R.string.cleanup_settings_title), onBack = onBack)
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
            Text(stringResource(R.string.save_settings))
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
        TranscriptionProvider.DEEPGRAM -> {
            SecretField(stringResource(R.string.api_key_label), settings.deepgramApiKey) {
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

@Composable
private fun SearchField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String? = null,
    contentDescription: String? = null,
) {
    val resolvedPlaceholder = placeholder ?: stringResource(R.string.search_placeholder_default)
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
        placeholder = { Text(resolvedPlaceholder) },
        singleLine = true,
        modifier = fieldModifier,
    )
}

@Composable
private fun DictionaryScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.dictionaryRepository.list()) }
    val context = LocalContext.current
    val savedMessage = stringResource(R.string.dictionary_saved)
    val deletedMessage = stringResource(R.string.dictionary_deleted)

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
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
        },
        onDelete = { entry ->
            container.dictionaryRepository.delete(entry.id)
            refresh()
            Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
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
    val addContentDescription = stringResource(R.string.dictionary_add_content_description)
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
                title = stringResource(R.string.dictionary_title),
                subtitle = stringResource(R.string.dictionary_subtitle),
            )
            SearchField(
                value = query,
                onChange = onQueryChange,
                placeholder = stringResource(R.string.dictionary_search_placeholder),
                contentDescription = stringResource(R.string.dictionary_search_content_description),
            )
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateBlock(
                        title = if (query.isBlank()) {
                            stringResource(R.string.dictionary_empty_title)
                        } else {
                            stringResource(R.string.dictionary_empty_search_title)
                        },
                        description = if (query.isBlank()) {
                            stringResource(R.string.dictionary_empty_description)
                        } else {
                            stringResource(R.string.dictionary_empty_search_description)
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
                .semantics { contentDescription = addContentDescription },
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
    val editContentDescription = stringResource(R.string.edit_item_content_description, entry.term)
    val deleteContentDescription = stringResource(R.string.delete_item_content_description, entry.term)
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
                    modifier = Modifier.semantics {
                        contentDescription = editContentDescription
                    },
                ) {
                    Text(stringResource(R.string.edit))
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics {
                        contentDescription = deleteContentDescription
                    },
                ) {
                    Text(stringResource(R.string.delete))
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
    val termInputContentDescription = stringResource(R.string.dictionary_term_input_content_description)
    val noteInputContentDescription = stringResource(R.string.dictionary_note_input_content_description)
    var term by remember(entry?.id) { mutableStateOf(entry?.term.orEmpty()) }
    var note by remember(entry?.id) { mutableStateOf(entry?.note.orEmpty()) }
    val title = if (entry == null) {
        stringResource(R.string.dictionary_dialog_add_title)
    } else {
        stringResource(R.string.dictionary_dialog_edit_title)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = term,
                    onValueChange = { term = it },
                    label = { Text(stringResource(R.string.dictionary_term_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = termInputContentDescription },
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.dictionary_note_label)) },
                    placeholder = { Text(stringResource(R.string.dictionary_note_placeholder)) },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = noteInputContentDescription },
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun SnippetsScreen(container: VerballyContainer, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var entries by remember { mutableStateOf(container.snippetRepository.list()) }
    val context = LocalContext.current
    val savedMessage = stringResource(R.string.snippets_saved)
    val deletedMessage = stringResource(R.string.snippets_deleted)

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
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
        },
        onDelete = { entry ->
            container.snippetRepository.delete(entry.id)
            refresh()
            Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
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
    val addContentDescription = stringResource(R.string.snippets_add_content_description)
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
                title = stringResource(R.string.snippets_title),
                subtitle = stringResource(R.string.snippets_subtitle),
            )
            SearchField(
                value = query,
                onChange = onQueryChange,
                placeholder = stringResource(R.string.snippets_search_placeholder),
                contentDescription = stringResource(R.string.snippets_search_content_description),
            )
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateBlock(
                        title = if (query.isBlank()) {
                            stringResource(R.string.snippets_empty_title)
                        } else {
                            stringResource(R.string.snippets_empty_search_title)
                        },
                        description = if (query.isBlank()) {
                            stringResource(R.string.snippets_empty_description)
                        } else {
                            stringResource(R.string.snippets_empty_search_description)
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
                .semantics { contentDescription = addContentDescription },
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
    val editContentDescription = stringResource(R.string.edit_item_content_description, entry.trigger)
    val deleteContentDescription = stringResource(R.string.delete_item_content_description, entry.trigger)
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
                    modifier = Modifier.semantics {
                        contentDescription = editContentDescription
                    },
                ) {
                    Text(stringResource(R.string.edit))
                }
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.semantics {
                        contentDescription = deleteContentDescription
                    },
                ) {
                    Text(stringResource(R.string.delete))
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
    val triggerInputContentDescription = stringResource(R.string.snippets_trigger_input_content_description)
    val expansionInputContentDescription = stringResource(R.string.snippets_expansion_input_content_description)
    var trigger by remember(entry?.id) { mutableStateOf(entry?.trigger.orEmpty()) }
    var expansion by remember(entry?.id) { mutableStateOf(entry?.expansion.orEmpty()) }
    val title = if (entry == null) {
        stringResource(R.string.snippets_dialog_add_title)
    } else {
        stringResource(R.string.snippets_dialog_edit_title)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = trigger,
                    onValueChange = { trigger = it },
                    label = { Text(stringResource(R.string.snippets_trigger_label)) },
                    placeholder = { Text(stringResource(R.string.snippets_trigger_placeholder)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = triggerInputContentDescription },
                )
                OutlinedTextField(
                    value = expansion,
                    onValueChange = { expansion = it },
                    label = { Text(stringResource(R.string.snippets_expansion_label)) },
                    placeholder = { Text(stringResource(R.string.snippets_expansion_placeholder)) },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = expansionInputContentDescription },
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun StyleProfilesScreen(
    container: VerballyContainer,
    modifier: Modifier = Modifier,
    onEditorActiveChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val ruleSavedMessage = stringResource(R.string.style_rule_saved)
    val ruleRestoredMessage = stringResource(R.string.style_rule_restored)
    val styleSavedMessage = stringResource(R.string.style_saved)
    var profiles by remember { mutableStateOf(container.styleProfileRepository.list()) }
    val styleRuleLanguage = remember(context) {
        context.defaultPromptLanguageFor(container.settingsRepository.load().interfaceLanguage)
            .normalizedStyleRuleLanguage()
    }
    var styleRules by remember(styleRuleLanguage) {
        mutableStateOf(
            OutputStyle.entries.map { style ->
                container.styleRuleRepository.ruleFor(styleRuleLanguage, style)
            },
        )
    }
    var editingStyle by remember { mutableStateOf<OutputStyle?>(null) }
    fun refreshStyleRules() {
        styleRules = OutputStyle.entries.map { style ->
            container.styleRuleRepository.ruleFor(styleRuleLanguage, style)
        }
    }

    val currentEditingStyle = editingStyle
    LaunchedEffect(currentEditingStyle) {
        onEditorActiveChange(currentEditingStyle != null)
    }
    if (currentEditingStyle != null) {
        val editingRule = styleRules.first { it.style == currentEditingStyle }
        StyleRuleEditorScreen(
            language = styleRuleLanguage,
            rule = editingRule,
            onBack = { editingStyle = null },
            onSave = { ruleText ->
                container.styleRuleRepository.saveCustomRule(styleRuleLanguage, currentEditingStyle, ruleText)
                refreshStyleRules()
                Toast.makeText(context, ruleSavedMessage, Toast.LENGTH_SHORT).show()
                editingStyle = null
            },
            onRestoreDefault = {
                container.styleRuleRepository.restoreDefault(styleRuleLanguage, currentEditingStyle)
                refreshStyleRules()
                Toast.makeText(context, ruleRestoredMessage, Toast.LENGTH_SHORT).show()
            },
            modifier = modifier,
        )
        return
    }

    StyleProfilesScreenContent(
        profiles = profiles,
        styleRules = styleRules,
        onProfileChange = { profile ->
            container.styleProfileRepository.save(profile)
            profiles = container.styleProfileRepository.list()
            Toast.makeText(context, styleSavedMessage, Toast.LENGTH_SHORT).show()
        },
        onEditRule = { editingStyle = it },
        modifier = modifier,
    )
}

@Composable
fun StyleProfilesScreenContent(
    profiles: List<AppStyleProfile>,
    onProfileChange: (AppStyleProfile) -> Unit,
    modifier: Modifier = Modifier,
    styleRules: List<AppStyleRule> = emptyList(),
    onEditRule: (OutputStyle) -> Unit = {},
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        ScreenHeader(
            title = stringResource(R.string.style_title),
            subtitle = stringResource(R.string.style_subtitle),
        )
        profiles.forEach { profile ->
            StyleProfileRow(
                profile = profile,
                onProfileChange = onProfileChange,
            )
        }
        if (styleRules.isNotEmpty()) {
            StyleRulesSection(
                rules = styleRules,
                onEditRule = onEditRule,
            )
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun StyleRulesSection(
    rules: List<AppStyleRule>,
    onEditRule: (OutputStyle) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.style_rules_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.style_rules_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            rules.forEachIndexed { index, rule ->
                StyleRuleRow(
                    rule = rule,
                    onClick = { onEditRule(rule.style) },
                )
                if (index < rules.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun StyleRuleRow(
    rule: AppStyleRule,
    onClick: () -> Unit,
) {
    val styleLabel = rule.style.localizedLabel()
    val contentDescription = stringResource(R.string.style_rule_row_content_description, styleLabel)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.style_rule_row_title, styleLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (rule.isCustom) {
                    stringResource(R.string.style_rule_status_custom)
                } else {
                    stringResource(R.string.style_rule_status_default)
                },
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
fun StyleRuleEditorScreen(
    language: AppLanguage,
    rule: AppStyleRule,
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    onRestoreDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val styleLabel = rule.style.localizedLabel()
    val backContentDescription = stringResource(R.string.style_rule_back)
    val menuContentDescription = stringResource(R.string.style_rule_menu_content_description, styleLabel)
    val textContentDescription = stringResource(R.string.style_rule_text_content_description, styleLabel)
    var ruleText by remember(language, rule.style, rule.rule) { mutableStateOf(rule.rule) }
    var menuExpanded by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenHorizontalPadding, vertical = ScreenVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .offset(x = (-16).dp)
                        .semantics {
                            contentDescription = backContentDescription
                        },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_app_arrow_back_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = stringResource(R.string.style_rule_editor_title, styleLabel),
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = (-16).dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier
                            .offset(x = 16.dp)
                            .semantics {
                                contentDescription = menuContentDescription
                            },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_app_more_vert_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.restore_default)) },
                            onClick = {
                                menuExpanded = false
                                onRestoreDefault()
                                ruleText = StyleRuleDefaults.defaultRuleFor(language, rule.style)
                            },
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.style_rule_editor_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.style_rule_language_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = language.localizedLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        OutlinedTextField(
            value = ruleText,
            onValueChange = { ruleText = it },
            label = { Text(stringResource(R.string.style_rule_text_label)) },
            minLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = textContentDescription
                },
        )
        Button(
            onClick = { onSave(ruleText) },
            modifier = Modifier
                .fillMaxWidth()
                .height(PrimaryActionHeight),
            enabled = ruleText.trim().isNotEmpty(),
        ) {
            Text(stringResource(R.string.style_rule_save_button, styleLabel))
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun StyleProfileRow(
    profile: AppStyleProfile,
    onProfileChange: (AppStyleProfile) -> Unit,
) {
    val categoryLabel = profile.category.localizedLabel()
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
                text = categoryLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = profile.category.localizedDescription(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutputStyle.entries.forEach { outputStyle ->
                    val selected = profile.style == outputStyle
                    val styleLabel = outputStyle.localizedLabel()
                    val contentDescription = stringResource(
                        R.string.style_option_content_description,
                        categoryLabel,
                        styleLabel,
                    )
                    val buttonModifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .semantics {
                            this.contentDescription = contentDescription
                        }
                    if (selected) {
                        Button(
                            onClick = { onProfileChange(profile.copy(style = outputStyle)) },
                            modifier = buttonModifier,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text(styleLabel)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onProfileChange(profile.copy(style = outputStyle)) },
                            modifier = buttonModifier,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) {
                            Text(styleLabel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCategory.localizedLabel(): String = when (this) {
    AppCategory.CHAT -> stringResource(R.string.style_category_chat)
    AppCategory.WORK -> stringResource(R.string.style_category_work)
    AppCategory.OTHER -> stringResource(R.string.style_category_other)
}

@Composable
private fun AppCategory.localizedDescription(): String = when (this) {
    AppCategory.CHAT -> stringResource(R.string.style_category_chat_description)
    AppCategory.WORK -> stringResource(R.string.style_category_work_description)
    AppCategory.OTHER -> stringResource(R.string.style_category_other_description)
}

@Composable
private fun OutputStyle.localizedLabel(): String = when (this) {
    OutputStyle.FORMAL -> stringResource(R.string.output_style_formal)
    OutputStyle.CASUAL -> stringResource(R.string.output_style_casual)
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
    val historyClearedMessage = stringResource(R.string.history_cleared)
    val copiedMessage = stringResource(R.string.copied)

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
            Toast.makeText(context, historyClearedMessage, Toast.LENGTH_SHORT).show()
        },
        onCopy = { entry ->
            copyText(context, entry.cleanedText)
            Toast.makeText(context, copiedMessage, Toast.LENGTH_SHORT).show()
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
            title = { Text(stringResource(R.string.history_clear_confirm_title)) },
            text = { Text(stringResource(R.string.history_clear_confirm_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmation = false
                        onClearHistory()
                    },
                ) {
                    Text(stringResource(R.string.history_clear_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
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
        SearchField(
            value = query,
            onChange = onQueryChange,
            placeholder = stringResource(R.string.history_search_placeholder),
        )
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
                            title = stringResource(R.string.history_empty_title),
                            description = stringResource(R.string.history_empty_description),
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
    val historyMenuContentDescription = stringResource(R.string.history_menu_content_description)
    Box(modifier = Modifier.fillMaxWidth()) {
        ScreenHeader(
            title = stringResource(R.string.history_title),
            subtitle = stringResource(R.string.history_subtitle),
        )
        if (showOverflow) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-6).dp),
            ) {
                IconButton(
                    onClick = onOverflowClick,
                    modifier = Modifier.semantics {
                        contentDescription = historyMenuContentDescription
                    },
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
                                text = stringResource(R.string.history_clear_menu_item),
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
                TextButton(onClick = onCopy) { Text(stringResource(R.string.copy)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.app_name), text))
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
            "package:${context.packageName}".toUri(),
        ),
    )
}
