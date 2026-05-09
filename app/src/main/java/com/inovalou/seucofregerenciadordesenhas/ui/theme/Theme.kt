package com.inovalou.seucofregerenciadordesenhas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference

private val DarkColorScheme = darkColorScheme(
    primary = DarkVaultColors.primary,
    secondary = DarkVaultColors.secondary,
    tertiary = DarkVaultColors.primaryDim,
    background = DarkVaultColors.background,
    surface = DarkVaultColors.background,
    surfaceContainer = DarkVaultColors.surface,
    surfaceContainerHigh = DarkVaultColors.surfaceHigh,
    surfaceContainerHighest = DarkVaultColors.surfaceHighest,
    surfaceBright = DarkVaultColors.surfaceBright,
    onPrimary = DarkVaultColors.onAccent,
    onSecondary = DarkVaultColors.onAccent,
    onBackground = DarkVaultColors.textPrimary,
    onSurface = DarkVaultColors.textPrimary,
    onSurfaceVariant = DarkVaultColors.textSecondary,
    outlineVariant = DarkVaultColors.outline,
    error = DarkVaultColors.danger
)

private val LightColorScheme = lightColorScheme(
    primary = LightVaultColors.primary,
    secondary = LightVaultColors.secondary,
    tertiary = LightVaultColors.primaryDim,
    background = LightVaultColors.background,
    surface = LightVaultColors.background,
    surfaceContainer = LightVaultColors.surface,
    surfaceContainerHigh = LightVaultColors.surfaceHigh,
    surfaceContainerHighest = LightVaultColors.surfaceHighest,
    surfaceBright = LightVaultColors.surfaceBright,
    onPrimary = LightVaultColors.surface,
    onSecondary = LightVaultColors.surface,
    onBackground = LightVaultColors.textPrimary,
    onSurface = LightVaultColors.textPrimary,
    onSurfaceVariant = LightVaultColors.textSecondary,
    outlineVariant = LightVaultColors.outline,
    error = LightVaultColors.danger
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
    val vaultColors = if (useDarkTheme) DarkVaultColors else LightVaultColors

    CompositionLocalProvider(LocalVaultColors provides vaultColors) {
        MaterialTheme(
            colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
