package com.verbally.app.insertion

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo

class AndroidClipboardGateway(
    private val context: Context,
) : ClipboardGateway {
    private val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    override var currentText: String?
        get() = clipboard.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
        set(value) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Verbally", value.orEmpty()))
        }
}

class AccessibilityPasteTarget(
    private val service: AccessibilityService,
) : PasteTarget {
    override fun pasteFromClipboard(text: String): Boolean {
        val focusedNode = service.rootInActiveWindow
            ?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: service.rootInActiveWindow?.findEditableNode()
        return focusedNode?.performAction(AccessibilityNodeInfo.ACTION_PASTE) == true
    }
}

fun AccessibilityNodeInfo.findEditableNode(): AccessibilityNodeInfo? {
    if (isEditable) return this
    for (index in 0 until childCount) {
        val found = getChild(index)?.findEditableNode()
        if (found != null) return found
    }
    return null
}
