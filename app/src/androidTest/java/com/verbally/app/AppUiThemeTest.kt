package com.verbally.app

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.verbally.app.settings.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppUiThemeTest {
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
    fun verballyThemeUsesSelectedLightAndDarkSchemes() {
        var lightPrimary = Color.Unspecified
        var lightBackground = Color.Unspecified
        var lightSecondaryContainer = Color.Unspecified
        var lightTertiaryContainer = Color.Unspecified
        var darkBackground = Color.Unspecified
        var darkSecondaryContainer = Color.Unspecified
        var darkTertiaryContainer = Color.Unspecified

        composeRule.setContent {
            VerballyTheme(themeMode = AppThemeMode.LIGHT) {
                lightPrimary = MaterialTheme.colorScheme.primary
                lightBackground = MaterialTheme.colorScheme.background
                lightSecondaryContainer = MaterialTheme.colorScheme.secondaryContainer
                lightTertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
            }
            VerballyTheme(themeMode = AppThemeMode.DARK) {
                darkBackground = MaterialTheme.colorScheme.background
                darkSecondaryContainer = MaterialTheme.colorScheme.secondaryContainer
                darkTertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
            }
        }

        composeRule.runOnIdle {
            assertEquals(Color(0xFF14233A), lightPrimary)
            assertEquals(Color(0xFFF8FAFC), lightBackground)
            assertEquals(Color(0xFFD6F1EA), lightSecondaryContainer)
            assertEquals(Color(0xFFEDE7FF), lightTertiaryContainer)
            assertEquals(Color(0xFF0F1419), darkBackground)
            assertEquals(Color(0xFF234E48), darkSecondaryContainer)
            assertEquals(Color(0xFF564A88), darkTertiaryContainer)
        }
    }

}
