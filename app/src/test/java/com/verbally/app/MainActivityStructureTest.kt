package com.verbally.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityStructureTest {
    @Test
    fun mainActivityStaysFocusedOnActivityEntryPoint() {
        val mainActivity = sourceFile("app/src/main/java/com/verbally/app/MainActivity.kt")
        val lineCount = mainActivity.readLines().size

        assertTrue(
            "MainActivity.kt should stay focused on the Activity entry point; actual lines=$lineCount",
            lineCount <= 250,
        )
    }

    private fun sourceFile(path: String): File =
        listOf(
            File(path),
            File("../$path"),
        ).firstOrNull { it.isFile } ?: error("Missing source file $path")
}
