package com.verbally.app.system

import android.text.InputType

data class SensitiveInputContext(
    val packageName: String?,
    val isPassword: Boolean,
    val inputType: Int,
)

class SensitiveInputPolicy(
    private val financialPackages: Set<String> = DEFAULT_FINANCIAL_PACKAGES,
) {
    fun isSensitive(context: SensitiveInputContext): Boolean =
        context.isPassword ||
            isSensitiveInputType(context.inputType) ||
            isKnownFinancialPackage(context.packageName)

    private fun isSensitiveInputType(inputType: Int): Boolean {
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return inputClass == InputType.TYPE_CLASS_NUMBER ||
            inputClass == InputType.TYPE_CLASS_PHONE ||
            variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
    }

    private fun isKnownFinancialPackage(packageName: String?): Boolean {
        val name = packageName.orEmpty().lowercase()
        return name in financialPackages ||
            financialPackages.any { blocked -> name.startsWith("$blocked.") }
    }

    private companion object {
        val DEFAULT_FINANCIAL_PACKAGES = setOf(
            "com.chase.sig.android",
            "com.bankofamerica.digitalwallet",
            "com.wf.wellsfargomobile",
            "com.citi.citimobile",
            "com.usaa.mobile.android.usaa",
            "com.paypal.android.p2pmobile",
            "com.venmo",
            "com.robinhood.android",
            "com.coinbase.android",
            "tw.com.taishinbank.mobile",
            "com.ctbcbank",
            "com.esunbank",
            "com.cathaybk.mymobibank.android",
            "com.fubon.mbank",
        )
    }
}

data class SensitiveInputObservation(
    val sensitive: Boolean,
    val hasInputMetadata: Boolean,
    val inputMethodVisible: Boolean,
)

class SensitiveInputState {
    private var activeInputSensitive = false

    fun resolve(observation: SensitiveInputObservation): Boolean {
        if (observation.sensitive) {
            activeInputSensitive = true
        } else if (observation.hasInputMetadata || !observation.inputMethodVisible) {
            activeInputSensitive = false
        }
        return observation.sensitive || activeInputSensitive
    }
}
