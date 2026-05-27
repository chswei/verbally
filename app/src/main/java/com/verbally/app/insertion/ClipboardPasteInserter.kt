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
) {
    suspend fun insert(text: String): InsertResult {
        if (directTextTarget.insertDirectly(text)) {
            return InsertResult(pasted = true, message = "已貼上。")
        }

        clipboard.currentText = text
        return InsertResult(pasted = false, message = "請手動貼上，文字已複製到剪貼簿。")
    }
}
