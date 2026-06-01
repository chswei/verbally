package com.verbally.app

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PermissionScreensTest {
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
    fun drawerPermissionSupportStaysOpenWhenPermissionsAreAlreadyComplete() {
        assertFalse(
            shouldDismissPermissionScreenOnPermissionRefresh(
                openReason = PermissionScreenOpenReason.SUPPORT,
                permissionsReady = true,
            ),
        )
        assertTrue(
            shouldDismissPermissionScreenOnPermissionRefresh(
                openReason = PermissionScreenOpenReason.REQUIRED_SETUP,
                permissionsReady = true,
            ),
        )
        assertFalse(
            shouldDismissPermissionScreenOnPermissionRefresh(
                openReason = PermissionScreenOpenReason.REQUIRED_SETUP,
                permissionsReady = false,
            ),
        )
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
