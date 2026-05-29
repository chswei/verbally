package com.verbally.app.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeModeTest {
    @Test
    fun exposesTraditionalChineseLabelsInSettingsOrder() {
        assertEquals(
            listOf("跟隨系統", "淺色", "深色"),
            AppThemeMode.entries.map { it.label },
        )
    }

    @Test
    fun appSettingsDefaultsToFollowingSystemThemeMode() {
        assertEquals(AppThemeMode.SYSTEM, AppSettings().themeMode)
    }

    @Test
    fun storedThemeModeNamesFallBackToSystemWhenInvalid() {
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStoredName("DARK"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStoredName(null))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStoredName("missing"))
    }
}
