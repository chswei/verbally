package com.verbally.app.system

import android.Manifest
import androidx.test.platform.app.InstrumentationRegistry
import com.verbally.app.audio.TemporaryAudioRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.*
import org.junit.Test

class DictationRecordingTeardownTest {
    @Test fun teardownDeletesRecordingAndRecorderCanBeReused() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.RECORD_AUDIO)
        val recorder = TemporaryAudioRecorder(context)
        val lifetime = DictationServiceLifetime(CoroutineScope(SupervisorJob() + Dispatchers.Main)) {
            recorder.stopAndDelete()
        }
        try {
            val first = recorder.start()
            assertTrue(first.exists())
            lifetime.close()
            lifetime.close()
            assertFalse(first.exists())
            assertNull(recorder.stop())
            val second = recorder.start()
            assertTrue(second.exists())
            recorder.stopAndDelete()
            assertFalse(second.exists())
        } finally {
            lifetime.close()
            recorder.stopAndDelete()
        }
    }
}
