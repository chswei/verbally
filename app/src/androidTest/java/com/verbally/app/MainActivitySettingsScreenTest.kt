package com.verbally.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.verbally.app.settings.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainActivitySettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longApiKeysKeepSaveActionReachableByScrolling() {
        val longApiKey = "sk-" + "a".repeat(240)

        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(
                        openAiApiKey = longApiKey,
                        geminiApiKey = longApiKey,
                    ),
                    onSettingsChange = {},
                    onSave = {},
                    onClearHistory = {},
                    onOpenTranscriptionSettings = {},
                    onOpenCleanupSettings = {},
                )
            }
        }

        composeRule.onNodeWithText("清空歷史")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun settingsOverviewShowsApiSubpagesWithoutInlineFields() {
        composeRule.setContent {
            MaterialTheme {
                SettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                    onClearHistory = {},
                    onOpenTranscriptionSettings = {},
                    onOpenCleanupSettings = {},
                )
            }
        }

        composeRule.onNodeWithText("Transcribe")
            .assertIsDisplayed()
        composeRule.onNodeWithText("第二層處理")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("OpenAI API Key")
            .assertCountEquals(0)
    }

    @Test
    fun transcriptionSettingsOnlyShowsTranscriptionFields() {
        composeRule.setContent {
            MaterialTheme {
                TranscriptionSettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("OpenAI API Key")
            .assertIsDisplayed()
        composeRule.onNodeWithText("OpenAI 轉錄模型")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("Gemini API Key")
            .assertCountEquals(0)
    }

    @Test
    fun cleanupSettingsShowsProviderAndCleanupFields() {
        composeRule.setContent {
            MaterialTheme {
                CleanupSettingsScreenContent(
                    settings = AppSettings(),
                    onSettingsChange = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("整理 Provider")
            .assertIsDisplayed()
        composeRule.onNodeWithText("OpenAI 整理模型")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Gemini API Key")
            .assertIsDisplayed()
    }

    @Test
    fun mainAppShellUsesHistoryAndSettingsAsPrimaryDestinations() {
        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = true,
                    selectedDestination = AppDestination.HISTORY,
                    onDestinationSelected = {},
                    onOpenPermissions = {},
                    historyContent = {},
                    settingsContent = {},
                )
            }
        }

        composeRule.onNodeWithText("歷史")
            .assertIsDisplayed()
        composeRule.onNodeWithText("設定")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("權限")
            .assertCountEquals(0)
    }

    @Test
    fun missingPermissionsShowBannerActionInMainShell() {
        var openedPermissions = false

        composeRule.setContent {
            MaterialTheme {
                VerballyAppScaffold(
                    permissionsReady = false,
                    selectedDestination = AppDestination.HISTORY,
                    onDestinationSelected = {},
                    onOpenPermissions = { openedPermissions = true },
                    historyContent = {},
                    settingsContent = {},
                )
            }
        }

        composeRule.onNodeWithText("補開權限")
            .performClick()

        assertTrue(openedPermissions)
    }

    @Test
    fun permissionSetupOnlyShowsMicrophoneWhenItIsNext() {
        composeRule.setContent {
            MaterialTheme {
                PermissionSetupContent(
                    microphoneGranted = false,
                    overlayGranted = false,
                    accessibilityGranted = false,
                    primaryActionLabel = "開啟權限",
                    onContinue = {},
                    onOpenAppDetails = {},
                )
            }
        }

        composeRule.onNodeWithText("允許錄音")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("浮動視窗")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("輔助使用")
            .assertCountEquals(0)
    }

    @Test
    fun permissionSetupOnlyShowsOverlayWhenItIsNext() {
        composeRule.setContent {
            MaterialTheme {
                PermissionSetupContent(
                    microphoneGranted = true,
                    overlayGranted = false,
                    accessibilityGranted = false,
                    primaryActionLabel = "開啟設定",
                    onContinue = {},
                    onOpenAppDetails = {},
                )
            }
        }

        composeRule.onNodeWithText("允許浮動視窗")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("麥克風")
            .assertCountEquals(0)
        composeRule.onAllNodesWithText("輔助使用")
            .assertCountEquals(0)
    }

    @Test
    fun permissionResumeRefreshEffectRefreshesWhenAppReturnsToForeground() {
        val lifecycleOwner = TestLifecycleOwner()
        var refreshCount = 0

        composeRule.setContent {
            PermissionResumeRefreshEffect(lifecycleOwner = lifecycleOwner) {
                refreshCount += 1
            }
        }

        composeRule.runOnIdle {
            assertEquals(0, refreshCount)
            lifecycleOwner.registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        composeRule.runOnIdle {
            assertEquals(1, refreshCount)
        }
    }

    private class TestLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle
            get() = registry
    }
}
