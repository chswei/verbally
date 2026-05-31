package com.verbally.app

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbally.app.settings.AppThemeMode

private val VerballyBrandBlue = Color(0xFF14233A)
private val VerballySoftBlue = Color(0xFFE6EDF6)
private val VerballyAccentTeal = Color(0xFF2F6F68)
private val VerballyAccentMint = Color(0xFFD6F1EA)
private val VerballyAccentLavender = Color(0xFFEDE7FF)
private val VerballyPageBackground = Color(0xFFF8FAFC)
private val VerballySurface = Color(0xFFFFFFFF)
private val VerballyOutline = Color(0xFFD1D9E4)
internal val ScreenHorizontalPadding = 24.dp
internal val ScreenVerticalPadding = 12.dp
internal val FormFieldHeight = 56.dp
internal val ModelDropdownHeight = 64.dp
internal val PrimaryActionHeight = 52.dp
internal val SettingsChoiceRowHeight = 48.dp
private val VerballyColorScheme = lightColorScheme(
    primary = VerballyBrandBlue,
    onPrimary = Color.White,
    primaryContainer = VerballySoftBlue,
    onPrimaryContainer = VerballyBrandBlue,
    secondary = VerballyAccentTeal,
    onSecondary = Color.White,
    secondaryContainer = VerballyAccentMint,
    onSecondaryContainer = Color(0xFF123532),
    tertiary = Color(0xFF6A5CA8),
    onTertiary = Color.White,
    tertiaryContainer = VerballyAccentLavender,
    onTertiaryContainer = Color(0xFF2B2456),
    background = VerballyPageBackground,
    onBackground = Color(0xFF171C22),
    surface = VerballySurface,
    onSurface = Color(0xFF171C22),
    surfaceVariant = Color(0xFFE8EDF4),
    onSurfaceVariant = Color(0xFF465464),
    outline = VerballyOutline,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)
private val VerballyDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB9C7E8),
    onPrimary = Color(0xFF243049),
    primaryContainer = Color(0xFF33415D),
    onPrimaryContainer = Color(0xFFDCE5FF),
    secondary = Color(0xFF9BCFC6),
    onSecondary = Color(0xFF063A35),
    secondaryContainer = Color(0xFF234E48),
    onSecondaryContainer = Color(0xFFB7ECE3),
    tertiary = Color(0xFFCFC2FF),
    onTertiary = Color(0xFF372D65),
    tertiaryContainer = Color(0xFF564A88),
    onTertiaryContainer = Color(0xFFE8DEFF),
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE0E2E8),
    surface = Color(0xFF1B1F24),
    onSurface = Color(0xFFE0E2E8),
    surfaceVariant = Color(0xFF42474F),
    onSurfaceVariant = Color(0xFFC3C7D0),
    outline = Color(0xFF8C919A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)
private val VerballyTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
)
private val VerballyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)
@Composable
fun VerballyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val colorScheme = if (useDarkTheme) VerballyDarkColorScheme else VerballyColorScheme
    VerballySystemBarEffect(
        useDarkTheme = useDarkTheme,
        statusBarColor = colorScheme.background,
        navigationBarColor = colorScheme.surface,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VerballyTypography,
        shapes = VerballyShapes,
        content = content,
    )
}

@Composable
private fun VerballySystemBarEffect(
    useDarkTheme: Boolean,
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    val view = LocalView.current
    SideEffect {
        if (view.isInEditMode) return@SideEffect
        val activity = view.context.findActivity() as? ComponentActivity ?: return@SideEffect
        val statusBarArgb = statusBarColor.toArgb()
        val navigationBarArgb = navigationBarColor.toArgb()
        activity.enableEdgeToEdge(
            statusBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(statusBarArgb)
            } else {
                SystemBarStyle.light(statusBarArgb, statusBarArgb)
            },
            navigationBarStyle = if (useDarkTheme) {
                SystemBarStyle.dark(navigationBarArgb)
            } else {
                SystemBarStyle.light(navigationBarArgb, navigationBarArgb)
            },
        )
    }
}
