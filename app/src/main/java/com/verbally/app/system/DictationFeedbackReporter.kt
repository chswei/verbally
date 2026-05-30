package com.verbally.app.system

import android.content.Context
import android.widget.Toast
import com.verbally.app.overlay.FloatingDictationOverlay

interface DictationOverlayFeedback {
    fun completeProcessing(message: String? = null)
}

interface UserMessageSink {
    fun show(message: String)
}

class DictationFeedbackReporter(
    private val overlay: DictationOverlayFeedback,
    private val userMessages: UserMessageSink,
) {
    fun reportSuccess() {
        overlay.completeProcessing()
    }

    fun reportFailure(message: String) {
        overlay.completeProcessing(message)
        userMessages.show(message)
    }
}

class FloatingDictationOverlayFeedback(
    private val overlay: FloatingDictationOverlay,
) : DictationOverlayFeedback {
    override fun completeProcessing(message: String?) {
        overlay.completeProcessing(message)
    }
}

class ToastUserMessageSink(
    private val context: Context,
) : UserMessageSink {
    override fun show(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
