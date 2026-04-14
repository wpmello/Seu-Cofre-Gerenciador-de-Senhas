package com.inovalou.seucofregerenciadordesenhas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun SeuCofreGerenciadorDeSenhasTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
