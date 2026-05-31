package com.verbally.app

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.os.LocaleList
import com.verbally.app.settings.AppLanguage

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun Context.applyAppLanguage(language: AppLanguage) {
    val localeManager = getSystemService(LocaleManager::class.java)
    localeManager.applicationLocales = if (language == AppLanguage.SYSTEM) {
        LocaleList.getEmptyLocaleList()
    } else {
        LocaleList.forLanguageTags(language.languageTag)
    }
}

internal fun Context.defaultPromptLanguageFor(language: AppLanguage): AppLanguage =
    AppLanguage.defaultPromptLanguageFor(
        selectedInterfaceLanguage = language,
        systemLanguageTag = getSystemService(LocaleManager::class.java).systemLocales[0]?.toLanguageTag(),
    )
