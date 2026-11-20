package com.verbally.app

import android.app.LocaleManager
import android.os.LocaleList
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource

/** Apply locale before Compose's Activity rule launches, avoiding recreation after setContent. */
class TraditionalChineseLocaleRule : ExternalResource() {
    override fun before() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.runOnMainSync {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags("zh-TW")
        }
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (context.resources.configuration.locales[0].toLanguageTag() != "zh-TW") {
            check(SystemClock.uptimeMillis() < deadline) { "Traditional Chinese resources did not become active" }
            instrumentation.waitForIdleSync()
            SystemClock.sleep(10)
        }
        instrumentation.waitForIdleSync()
    }
}
