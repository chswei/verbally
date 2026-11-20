package com.verbally.app.system

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

/** Owned by the service and closed on its main thread. */
internal class DictationServiceLifetime(
    private val scope: CoroutineScope,
    private val stopRecording: () -> Unit,
) {
    private var closed = false

    fun close() {
        if (closed) return
        closed = true
        scope.cancel()
        stopRecording()
    }
}
