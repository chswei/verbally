package com.verbally.app

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizedUiOverflowGuardTest {
    @Test
    fun bottomNavigationUsesCompactSingleLineLabels() {
        val source = sourceFile("app/src/main/java/com/verbally/app/MainShell.kt").readText()

        assertTrue(
            "Bottom navigation needs dedicated compact labels so long localized page names do not wrap.",
            source.contains("compactLabelRes"),
        )
        assertFalse(
            "Bottom navigation should not render full destination labels directly.",
            source.contains("label = { Text(stringResource(destination.labelRes)) }"),
        )
        assertTrue(
            "Bottom navigation labels should be single-line with ellipsis overflow.",
            source.contains("maxLines = 1") && source.contains("overflow = TextOverflow.Ellipsis"),
        )
    }

    @Test
    fun localizedActionButtonsAvoidFixedHeights() {
        val files = listOf(
            "app/src/main/java/com/verbally/app/SettingsScreens.kt",
            "app/src/main/java/com/verbally/app/PermissionScreens.kt",
            "app/src/main/java/com/verbally/app/StyleScreens.kt",
        )

        files.forEach { path ->
            val source = sourceFile(path).readText()
            assertFalse(
                "$path should use adaptive action buttons instead of fixed-height localized buttons.",
                source.contains(".height(PrimaryActionHeight)"),
            )
        }
    }

    @Test
    fun localizedChoiceRowsAvoidFixedHeights() {
        val source = sourceFile("app/src/main/java/com/verbally/app/SettingsScreens.kt").readText()

        assertFalse(
            "Settings choice rows should grow for long localized values instead of clipping them.",
            source.contains(".height(SettingsChoiceRowHeight)"),
        )
    }

    private fun sourceFile(path: String): File =
        listOf(
            File(path),
            File("../$path"),
        ).firstOrNull { it.isFile } ?: error("Missing source file $path")
}
