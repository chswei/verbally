package com.verbally.app.insertion

interface ClipboardGateway {
    var currentText: String?
}

interface DirectTextTarget {
    suspend fun insertDirectly(text: String): Boolean
}

data class InsertResult(
    val pasted: Boolean,
    val message: String,
)

class ClipboardPasteInserter(
    private val clipboard: ClipboardGateway,
    private val directTextTarget: DirectTextTarget,
    private val directInsertMessage: String = "已貼上。",
    private val clipboardFallbackMessage: String = "請手動貼上，文字已複製到剪貼簿。",
) {
    suspend fun insert(text: String): InsertResult {
        if (directTextTarget.insertDirectly(text)) {
            return InsertResult(pasted = true, message = directInsertMessage)
        }

        clipboard.currentText = text
        return InsertResult(pasted = false, message = clipboardFallbackMessage)
    }
}
