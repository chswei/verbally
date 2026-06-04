package com.verbally.app.settings

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EncryptedSettingsRepositorySecurityTest {
    @Test
    fun providerKeysDoNotFallBackToPlainSharedPreferences() {
        val source = sourceFile("app/src/main/java/com/verbally/app/settings/EncryptedSettingsRepository.kt")
            .readText()

        assertTrue(source.contains("EncryptedSharedPreferences.create"))
        assertTrue(source.contains("AES256_GCM"))
        assertTrue(source.contains("AES256_SIV"))
        assertFalse(source.contains("verbally_settings_fallback"))
        assertFalse(source.contains("context.getSharedPreferences"))
    }

    private fun sourceFile(path: String): File =
        listOf(
            File(path),
            File("../$path"),
        ).firstOrNull { it.isFile } ?: error("Missing source file $path")
}
