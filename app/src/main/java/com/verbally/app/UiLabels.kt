package com.verbally.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.verbally.app.settings.AppLanguage

@Composable
internal fun AppLanguage.localizedLabel(): String = when (this) {
    AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
    else -> label
}
