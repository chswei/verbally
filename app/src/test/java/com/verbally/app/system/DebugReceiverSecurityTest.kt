package com.verbally.app.system

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugReceiverSecurityTest {
    @Test
    fun debugInsertReceiverIsNotExported() {
        val source = sourceFile("app/src/main/java/com/verbally/app/system/VerballyAccessibilityService.kt")
            .readText()

        assertTrue(source.contains("Context.RECEIVER_NOT_EXPORTED"))
        assertFalse(source.contains("Context.RECEIVER_EXPORTED"))
    }

    private fun sourceFile(path: String): File =
        listOf(
            File(path),
            File("../$path"),
        ).firstOrNull { it.isFile } ?: error("Missing source file $path")
}
