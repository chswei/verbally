package com.verbally.app

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainShellNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun useTraditionalChineseResources() {
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags("zh-TW")
    }

    @Test
    fun mainAppShellUsesHistoryAndSettingsAsPrimaryDestinations() {
        var openedSettings = false
        var showingSettings by mutableStateOf(false)
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = true,
                    showingSettings = showingSettings,
                    selectedDestination = AppDestination.HOME,
                    onDestinationSelected = {},
                    onOpenSettings = {
                        openedSettings = true
                        showingSettings = true
                    },
                    onOpenPermissions = {},
                    homeContent = {},
                    dictionaryContent = {},
                    snippetsContent = {},
                    historyContent = {},
                    styleContent = {},
                    settingsContent = { Text(context.getString(R.string.settings_appearance_mode)) },
                )
            }
        }

        composeRule.onNodeWithText("Verbally")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.nav_home))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.nav_dictionary))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.nav_snippets))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.nav_history))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText(context.getString(R.string.nav_style))
            .assertCountEquals(1)
        composeRule.onAllNodesWithText("☰")
            .assertCountEquals(0)
        composeRule.onNodeWithContentDescription("開啟選單")
            .performClick()
        composeRule.onNodeWithText("管理與支援")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("選單")
            .assertCountEquals(0)
        composeRule.onNodeWithText("應用程式")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("設定選單項目")
            .assertIsDisplayed()
        composeRule.onNodeWithText("外觀與 App 偏好")
            .assertIsDisplayed()
        composeRule.onNodeWithText("支援")
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription("權限與疑難排解選單項目")
            .assertIsDisplayed()
        composeRule.onNodeWithText("權限與疑難排解")
            .assertIsDisplayed()
        composeRule.onNodeWithText("錄音、浮動視窗與輔助使用")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Permission Setup")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("API 與偏好")
            .assertCountEquals(0)
        composeRule.onNodeWithText("設定")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { openedSettings }
        assertTrue(openedSettings)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.settings_appearance_mode))
            .assertIsDisplayed()
    }

    @Test
    fun drawerPermissionSupportOpensPermissionSetup() {
        var openedPermissions = false

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = true,
                    showingSettings = false,
                    selectedDestination = AppDestination.HOME,
                    onDestinationSelected = {},
                    onOpenSettings = {},
                    onOpenPermissions = { openedPermissions = true },
                    homeContent = {},
                    dictionaryContent = {},
                    snippetsContent = {},
                    historyContent = {},
                    styleContent = {},
                    settingsContent = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("開啟選單")
            .performClick()
        composeRule.onNodeWithText("權限與疑難排解")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { openedPermissions }
        assertTrue(openedPermissions)
    }

    @Test
    fun missingPermissionsShowBannerActionInMainShell() {
        var openedPermissions = false

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = false,
                    showingSettings = false,
                    selectedDestination = AppDestination.HOME,
                    onDestinationSelected = {},
                    onOpenSettings = {},
                    onOpenPermissions = { openedPermissions = true },
                    homeContent = {},
                    dictionaryContent = {},
                    snippetsContent = {},
                    historyContent = {},
                    styleContent = {},
                    settingsContent = {},
                )
            }
        }

        composeRule.onNodeWithText("補開權限")
            .performClick()

        assertTrue(openedPermissions)
    }

}
