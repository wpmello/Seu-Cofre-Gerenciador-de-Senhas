package com.inovalou.seucofregerenciadordesenhas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class VaultColors(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    val surfaceBright: Color,
    val primary: Color,
    val primaryDim: Color,
    val secondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val outline: Color,
    val onAccent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val securityPoorGradientStart: Color,
    val securityPoorGradientEnd: Color,
    val securityModerateGradientStart: Color,
    val securityModerateGradientEnd: Color,
    val securityExcellentGradientStart: Color,
    val securityExcellentGradientEnd: Color
)

val DarkVaultColors = VaultColors(
    background = MidnightBlue,
    surface = DeepNavy,
    surfaceHigh = SlateBlue,
    surfaceHighest = SlateBlue,
    surfaceBright = SurfaceBright,
    primary = ElectricBlue,
    primaryDim = ElectricBlueDim,
    secondary = NeonPink,
    textPrimary = SoftWhite,
    textSecondary = MistText,
    outline = GhostOutline,
    onAccent = MidnightBlue,
    success = VaultGreen,
    warning = VaultAmber,
    danger = VaultDanger,
    securityPoorGradientStart = VaultDanger,
    securityPoorGradientEnd = Color(0xFF9F0519),
    securityModerateGradientStart = VaultAmber,
    securityModerateGradientEnd = Color(0xFFFFE39A),
    securityExcellentGradientStart = VaultGreen,
    securityExcellentGradientEnd = Color(0xFF9AFFC4)
)

val LightVaultColors = VaultColors(
    background = Color(0xFFF7F9FE),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = ColorLightSurfaceContainer,
    surfaceHighest = ColorLightSurfaceContainerHigh,
    surfaceBright = ColorLightSurfaceContainerHighest,
    primary = ElectricBlueDim,
    primaryDim = Color(0xFF0A56C5),
    secondary = Color(0xFFA326BA),
    textPrimary = MidnightBlue,
    textSecondary = ColorLightTextMuted,
    outline = Color(0xFFB8C3D6),
    onAccent = MidnightBlue,
    success = Color(0xFF147A3E),
    warning = Color(0xFF8A6400),
    danger = Color(0xFFB3261E),
    securityPoorGradientStart = Color(0xFFFF716C),
    securityPoorGradientEnd = Color(0xFFFFB4AE),
    securityModerateGradientStart = Color(0xFFFFC857),
    securityModerateGradientEnd = Color(0xFFFFE39A),
    securityExcellentGradientStart = Color(0xFF3FFF8B),
    securityExcellentGradientEnd = Color(0xFF9AFFC4)
)

internal val LocalVaultColors = staticCompositionLocalOf { DarkVaultColors }

val MaterialTheme.vaultColors: VaultColors
    @Composable
    @ReadOnlyComposable
    get() = LocalVaultColors.current
