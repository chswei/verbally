package com.verbally.app.system

import org.junit.Assert.assertEquals
import org.junit.Test

class DictationFeedbackReporterTest {
    @Test
    fun successfulCompletionOnlyUpdatesOverlay() {
        val overlay = RecordingOverlayFeedback()
        val sink = RecordingUserMessageSink()
        val reporter = DictationFeedbackReporter(overlay = overlay, userMessages = sink)

        reporter.reportSuccess()

        assertEquals(listOf(null), overlay.messages)
        assertEquals(emptyList<String>(), sink.messages)
    }

    @Test
    fun failureUpdatesOverlayAndVisibleSink() {
        val overlay = RecordingOverlayFeedback()
        val sink = RecordingUserMessageSink()
        val reporter = DictationFeedbackReporter(overlay = overlay, userMessages = sink)

        reporter.reportFailure("OpenAI 轉錄失敗：invalid key")

        assertEquals(listOf("OpenAI 轉錄失敗：invalid key"), overlay.messages)
        assertEquals(listOf("OpenAI 轉錄失敗：invalid key"), sink.messages)
    }

    private class RecordingOverlayFeedback : DictationOverlayFeedback {
        val messages = mutableListOf<String?>()

        override fun completeProcessing(message: String?) {
            messages += message
        }
    }

    private class RecordingUserMessageSink : UserMessageSink {
        val messages = mutableListOf<String>()

        override fun show(message: String) {
            messages += message
        }
    }
}
