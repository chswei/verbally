package com.verbally.app

import androidx.annotation.StringRes

@StringRes
fun LocalEntrySaveResult.validationMessageRes(): Int? = when (this) {
    LocalEntrySaveResult.Saved -> null
    LocalEntrySaveResult.Invalid -> R.string.local_entry_validation_invalid
    LocalEntrySaveResult.Duplicate -> R.string.local_entry_validation_duplicate
    LocalEntrySaveResult.Conflict -> R.string.local_entry_validation_conflict
}
