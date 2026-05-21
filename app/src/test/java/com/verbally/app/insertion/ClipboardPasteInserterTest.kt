package com.verbally.app.insertion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardPasteInserterTest {
    @Test
    fun restoresPreviousClipboardWhenPasteSucceeds() {
        val clipboard = FakeClipboard("原本剪貼簿")
        val target = FakePasteTarget(shouldPaste = true)

        val result = ClipboardPasteInserter(clipboard, target).insert("整理後文字")

        assertTrue(result.pasted)
        assertEquals("整理後文字", target.pastedText)
        assertEquals("原本剪貼簿", clipboard.currentText)
    }

    @Test
    fun keepsCleanedTextOnClipboardWhenPasteFails() {
        val clipboard = FakeClipboard("原本剪貼簿")
        val target = FakePasteTarget(shouldPaste = false)

        val result = ClipboardPasteInserter(clipboard, target).insert("整理後文字")

        assertFalse(result.pasted)
        assertEquals("整理後文字", clipboard.currentText)
        assertEquals("請手動貼上，文字已複製到剪貼簿。", result.message)
    }

    private class FakeClipboard(initialText: String?) : ClipboardGateway {
        override var currentText: String? = initialText
    }

    private class FakePasteTarget(private val shouldPaste: Boolean) : PasteTarget {
        var pastedText: String? = null

        override fun pasteFromClipboard(text: String): Boolean {
            if (shouldPaste) {
                pastedText = text
            }
            return shouldPaste
        }
    }
}
