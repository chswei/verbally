package com.verbally.app.insertion

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.InputMethod
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay

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

class AccessibilityTextInsertionTarget(
    private val service: AccessibilityService,
) : DirectTextTarget {
    private val directTextCommitter = AccessibilityInputMethodTextCommitter(service)

    override suspend fun insertDirectly(text: String): Boolean =
        directTextCommitter.commitText(text)
}

internal interface DirectTextCommitter {
    suspend fun commitText(text: String): Boolean
}

internal interface DirectTextConnection {
    val editorPackageName: String?

    fun commitText(text: String)
    fun surroundingText(beforeLength: Int, afterLength: Int): CharSequence?
}

internal class VerifyingDirectTextCommitter(
    private val connectionProvider: () -> DirectTextConnection?,
    private val maxAttempts: Int = 3,
    private val surroundingTextLength: Int = 500,
    private val afterCommitDelay: suspend () -> Unit = { delay(100L) },
) : DirectTextCommitter {
    override suspend fun commitText(text: String): Boolean {
        repeat(maxAttempts) { index ->
            val attempt = index + 1
            val connection = connectionProvider()
            if (connection == null) {
                debugLog("InputConnection unavailable attempt=$attempt")
                return@repeat
            }

            val committed = runCatching {
                connection.commitText(text)
                true
            }.getOrElse { error ->
                debugLog(
                    "InputConnection commit failed package=${connection.editorPackageName} attempt=$attempt",
                    error,
                )
                false
            }
            if (!committed) return@repeat

            afterCommitDelay()
            val verified = runCatching {
                connection.surroundingText(
                    beforeLength = surroundingTextLength,
                    afterLength = surroundingTextLength,
                )?.contains(text) == true
            }.getOrElse { error ->
                debugLog(
                    "InputConnection verification failed package=${connection.editorPackageName} attempt=$attempt",
                    error,
                )
                false
            }
            if (verified) {
                debugLog(
                    "insert via InputConnection verified package=${connection.editorPackageName} attempt=$attempt",
                )
                return true
            }
            debugLog(
                "InputConnection commit not verified package=${connection.editorPackageName} attempt=$attempt",
            )
        }
        return false
    }
}

private class AccessibilityInputMethodTextCommitter(
    private val service: AccessibilityService,
) : DirectTextCommitter {
    private val verifyingCommitter = VerifyingDirectTextCommitter(
        connectionProvider = { service.currentDirectTextConnection() },
    )

    override suspend fun commitText(text: String): Boolean =
        verifyingCommitter.commitText(text)
}

private fun AccessibilityService.currentDirectTextConnection(): DirectTextConnection? {
    val inputMethod = runCatching { inputMethod }.getOrNull() ?: return null
    if (!inputMethod.currentInputStarted) return null
    val connection = inputMethod.currentInputConnection ?: return null
    return AccessibilityDirectTextConnection(inputMethod, connection)
}

private class AccessibilityDirectTextConnection(
    private val inputMethod: InputMethod,
    private val connection: InputMethod.AccessibilityInputConnection,
) : DirectTextConnection {
    override val editorPackageName: String?
        get() = inputMethod.currentInputEditorInfo?.packageName

    override fun commitText(text: String) {
        connection.commitText(text, 1, null)
    }

    override fun surroundingText(beforeLength: Int, afterLength: Int): CharSequence? =
        connection.getSurroundingText(beforeLength, afterLength, 0)?.text
}

private const val TAG = "VerballyInsertion"

private fun debugLog(message: String, error: Throwable? = null) {
    runCatching {
        if (error == null) {
            Log.d(TAG, message)
        } else {
            Log.d(TAG, message, error)
        }
    }
}
