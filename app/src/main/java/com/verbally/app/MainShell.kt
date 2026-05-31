package com.verbally.app

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.verbally.app.settings.AppSettings
import kotlinx.coroutines.launch

data class ApiKeyTestUiState(
    val isTesting: Boolean = false,
    val message: String? = null,
    val isSuccess: Boolean? = null,
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
