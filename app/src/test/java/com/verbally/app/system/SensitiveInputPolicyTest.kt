package com.verbally.app.system

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveInputPolicyTest {
    private val policy = SensitiveInputPolicy()

    @Test
    fun marksPasswordInputAsSensitive() {
        assertTrue(
            policy.isSensitive(
                SensitiveInputContext(
                    packageName = "jp.naver.line.android",
                    isPassword = true,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                ),
            ),
        )
    }

    @Test
    fun marksNumericAndPhoneInputsAsSensitive() {
        assertTrue(
            policy.isSensitive(
                SensitiveInputContext(
                    packageName = "com.google.android.keep",
                    isPassword = false,
                    inputType = InputType.TYPE_CLASS_NUMBER,
                ),
            ),
        )
        assertTrue(
            policy.isSensitive(
                SensitiveInputContext(
                    packageName = "com.google.android.keep",
                    isPassword = false,
                    inputType = InputType.TYPE_CLASS_PHONE,
                ),
            ),
        )
    }

    @Test
    fun marksKnownFinancialAppsAsSensitive() {
        assertTrue(
            policy.isSensitive(
                SensitiveInputContext(
                    packageName = "com.chase.sig.android",
                    isPassword = false,
                    inputType = InputType.TYPE_CLASS_TEXT,
                ),
            ),
        )
    }

    @Test
    fun allowsStandardTextInputsInNonSensitiveApps() {
        assertFalse(
            policy.isSensitive(
                SensitiveInputContext(
                    packageName = "jp.naver.line.android",
                    isPassword = false,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
                ),
            ),
        )
    }

    @Test
    fun remembersSensitiveInputAcrossMetadataFreeInputMethodEvents() {
        val state = SensitiveInputState()

        assertTrue(
            state.resolve(
                SensitiveInputObservation(
                    sensitive = true,
                    hasInputMetadata = true,
                    inputMethodVisible = true,
                ),
            ),
        )
        assertTrue(
            state.resolve(
                SensitiveInputObservation(
                    sensitive = false,
                    hasInputMetadata = false,
                    inputMethodVisible = true,
                ),
            ),
        )
    }

    @Test
    fun clearsSensitiveInputWhenNonSensitiveInputMetadataIsObserved() {
        val state = SensitiveInputState()
        state.resolve(
            SensitiveInputObservation(
                sensitive = true,
                hasInputMetadata = true,
                inputMethodVisible = true,
            ),
        )

        assertFalse(
            state.resolve(
                SensitiveInputObservation(
                    sensitive = false,
                    hasInputMetadata = true,
                    inputMethodVisible = true,
                ),
            ),
        )
    }

    @Test
    fun clearsSensitiveInputWhenInputMethodIsHidden() {
        val state = SensitiveInputState()
        state.resolve(
            SensitiveInputObservation(
                sensitive = true,
                hasInputMetadata = true,
                inputMethodVisible = true,
            ),
        )

        assertFalse(
            state.resolve(
                SensitiveInputObservation(
                    sensitive = false,
                    hasInputMetadata = false,
                    inputMethodVisible = false,
                ),
            ),
        )
    }
}
