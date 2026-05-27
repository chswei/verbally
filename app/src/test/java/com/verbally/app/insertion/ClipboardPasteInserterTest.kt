package com.verbally.app.insertion

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardPasteInserterTest {
    @Test
    fun usesDirectTextInsertionBeforeWritingClipboard() = runBlocking {
        val clipboard = FakeClipboard("原本剪貼簿")
        val target = FakeDirectTextTarget(
            clipboard = clipboard,
            directInsertSucceeds = true,
        )

        val result = ClipboardPasteInserter(clipboard, target).insert("整理後文字")

        assertTrue(result.pasted)
        assertEquals(listOf("原本剪貼簿"), target.clipboardTextsSeenDuringDirectInsert)
        assertEquals(emptyList<String>(), clipboard.writes)
        assertEquals("原本剪貼簿", clipboard.currentText)
    }

    @Test
    fun copiesCleanedTextToClipboardOnlyWhenDirectInsertionFails() = runBlocking {
        val clipboard = FakeClipboard("原本剪貼簿")
        val target = FakeDirectTextTarget(
            clipboard = clipboard,
            directInsertSucceeds = false,
        )

        val result = ClipboardPasteInserter(clipboard, target).insert("整理後文字")

        assertFalse(result.pasted)
        assertEquals(listOf("原本剪貼簿"), target.clipboardTextsSeenDuringDirectInsert)
        assertEquals(listOf("整理後文字"), clipboard.writes)
        assertEquals("整理後文字", clipboard.currentText)
        assertEquals("請手動貼上，文字已複製到剪貼簿。", result.message)
    }

    private class FakeClipboard(initialText: String?) : ClipboardGateway {
        val writes = mutableListOf<String?>()

        override var currentText: String? = initialText
            set(value) {
                writes += value
                field = value
            }
    }

    private class FakeDirectTextTarget(
        private val clipboard: ClipboardGateway,
        private val directInsertSucceeds: Boolean,
    ) : DirectTextTarget {
        val clipboardTextsSeenDuringDirectInsert = mutableListOf<String?>()

        override suspend fun insertDirectly(text: String): Boolean {
            clipboardTextsSeenDuringDirectInsert += clipboard.currentText
            return directInsertSucceeds
        }
    }
}
