package com.verbally.app.system

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class DictationServiceLifetimeTest {
    @Test fun shutdownCancelsProcessingAndStopsRecordingOnlyOnce() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        var stopped = 0
        var cleaned = false
        var inserted = false
        val lifetime = DictationServiceLifetime(scope) { stopped++ }
        val job = scope.launch {
            try {
                awaitCancellation()
                @Suppress("UNREACHABLE_CODE")
                inserted = true
            } finally { cleaned = true }
        }
        lifetime.close()
        lifetime.close()
        job.join()
        assertEquals(1, stopped)
        assertTrue(cleaned)
        assertTrue(job.isCancelled)
        assertFalse(inserted)
        assertFalse(scope.isActive)
    }
}
