package com.verbally.app.insertion

interface ClipboardGateway {
    var currentText: String?
}

interface PasteTarget {
    fun pasteFromClipboard(text: String): Boolean
}

data class InsertResult(
    val pasted: Boolean,
    val message: String,
)

class ClipboardPasteInserter(
    private val clipboard: ClipboardGateway,
    private val pasteTarget: PasteTarget,
) {
    fun insert(text: String): InsertResult {
        val previousText = clipboard.currentText
        clipboard.currentText = text

        return if (pasteTarget.pasteFromClipboard(text)) {
            clipboard.currentText = previousText
            InsertResult(pasted = true, message = "已貼上。")
        } else {
            InsertResult(pasted = false, message = "請手動貼上，文字已複製到剪貼簿。")
        }
    }
}
