package com.verbally.app

sealed class DictationOutcome {
    data object NoDictatedContent : DictationOutcome()

    data class Inserted(
        val message: String,
    ) : DictationOutcome()

    data class ClipboardFallback(
        val message: String,
    ) : DictationOutcome()

    data class Failure(
        val message: String,
    ) : DictationOutcome()
}
