package com.verbally.app.style

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.verbally.app.settings.AppLanguage

enum class AppCategory(val label: String) {
    CHAT("聊天"),
    WORK("工作"),
    OTHER("其他"),
}

enum class OutputStyle(val label: String) {
    FORMAL("Formal"),
    CASUAL("Casual"),
}

data class AppStyleProfile(
    val category: AppCategory,
    val style: OutputStyle,
)

data class CleanupStyleContext(
    val category: AppCategory,
    val style: OutputStyle,
    val language: AppLanguage = AppLanguage.TRADITIONAL_CHINESE,
    val customRule: String? = null,
) {
    companion object {
        fun default(): CleanupStyleContext =
            CleanupStyleContext(category = AppCategory.OTHER, style = OutputStyle.FORMAL)
    }
}

interface AppStyleProfileRepository {
    fun list(): List<AppStyleProfile>
    fun styleFor(category: AppCategory): OutputStyle
    fun save(profile: AppStyleProfile)
}

class InMemoryAppStyleProfileRepository : AppStyleProfileRepository {
    private val styles = defaultStyles.toMutableMap()

    override fun list(): List<AppStyleProfile> =
        AppCategory.entries.map { category ->
            AppStyleProfile(category = category, style = styleFor(category))
        }

    override fun styleFor(category: AppCategory): OutputStyle =
        styles[category] ?: defaultStyles.getValue(category)

    override fun save(profile: AppStyleProfile) {
        styles[profile.category] = profile.style
    }
}

class SharedPreferencesAppStyleProfileRepository(
    context: Context,
) : AppStyleProfileRepository {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("verbally_app_style_profiles", Context.MODE_PRIVATE)

    override fun list(): List<AppStyleProfile> =
        AppCategory.entries.map { category ->
            AppStyleProfile(category = category, style = styleFor(category))
        }

    override fun styleFor(category: AppCategory): OutputStyle {
        val saved = prefs.getString(keyFor(category), null)
        return saved?.let { raw ->
            OutputStyle.entries.firstOrNull { it.name == raw }
        } ?: defaultStyles.getValue(category)
    }

    override fun save(profile: AppStyleProfile) {
        prefs.edit {
            putString(keyFor(profile.category), profile.style.name)
        }
    }

    private fun keyFor(category: AppCategory): String =
        "${category.name.lowercase()}_style"
}

object AppCategoryClassifier {
    fun classify(packageName: String?): AppCategory {
        val normalized = packageName?.trim()?.lowercase().orEmpty()
        if (normalized.isBlank()) return AppCategory.OTHER
        if (normalized in chatPackages) return AppCategory.CHAT
        if (normalized in workPackages) return AppCategory.WORK
        return AppCategory.OTHER
    }

    private val chatPackages = setOf(
        "jp.naver.line.android",
        "com.facebook.orca",
        "com.whatsapp",
        "org.telegram.messenger",
        "com.instagram.android",
        "com.discord",
    )

    private val workPackages = setOf(
        "com.google.android.gm",
        "com.microsoft.office.outlook",
        "com.slack",
        "com.microsoft.teams",
        "com.google.android.apps.docs.editors.docs",
        "notion.id",
        "com.microsoft.office.word",
        "com.asana.app",
        "com.trello",
        "com.atlassian.android.jira.core",
        "app.linear",
    )
}

private val defaultStyles = mapOf(
    AppCategory.CHAT to OutputStyle.CASUAL,
    AppCategory.WORK to OutputStyle.FORMAL,
    AppCategory.OTHER to OutputStyle.FORMAL,
)
