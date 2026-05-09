package com.inovalou.seucofregerenciadordesenhas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonPink,
    tertiary = ElectricBlue,
    background = MidnightBlue,
    surface = MidnightBlue,
    surfaceContainer = DeepNavy,
    surfaceContainerHigh = SlateBlue,
    surfaceContainerHighest = SlateBlue,
    onPrimary = MidnightBlue,
    onSecondary = MidnightBlue,
    onBackground = SoftWhite,
    onSurface = SoftWhite,
    onSurfaceVariant = MistText,
    outlineVariant = GhostOutline
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlueDim,
    secondary = NeonPink,
    tertiary = ElectricBlue,
    background = SoftWhite,
    surface = SoftWhite,
    surfaceContainer = ColorLightSurfaceContainer,
    surfaceContainerHigh = ColorLightSurfaceContainerHigh,
    surfaceContainerHighest = ColorLightSurfaceContainerHighest,
    onPrimary = SoftWhite,
    onSecondary = MidnightBlue,
    onBackground = MidnightBlue,
    onSurface = MidnightBlue,
    onSurfaceVariant = ColorLightTextMuted,
    outlineVariant = GhostOutline
)

@Composable
fun SeuCofreGerenciadorDeSenhasTheme(
    themePreference: AppThemePreference = AppThemePreference.Dark,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = when (themePreference) {
        AppThemePreference.Dark -> true
        AppThemePreference.Light -> false
        AppThemePreference.System -> systemInDarkTheme
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
