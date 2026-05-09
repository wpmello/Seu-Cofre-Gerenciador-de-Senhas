package com.inovalou.seucofregerenciadordesenhas.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class VaultColorsTest {

    @Test
    fun givenDarkPalette_whenReadingVaultTokens_thenPreservesOriginalDarkIdentity() {
        assertEquals(MidnightBlue, DarkVaultColors.background)
        assertEquals(DeepNavy, DarkVaultColors.surface)
        assertEquals(SlateBlue, DarkVaultColors.surfaceHigh)
        assertEquals(SoftWhite, DarkVaultColors.textPrimary)
        assertEquals(MistText, DarkVaultColors.textSecondary)
        assertEquals(ElectricBlue, DarkVaultColors.primary)
        assertEquals(NeonPink, DarkVaultColors.secondary)
    }

    @Test
    fun givenLightPalette_whenReadingVaultTokens_thenUsesThemeSpecificSurfacesAndText() {
        assertNotEquals(DarkVaultColors.background, LightVaultColors.background)
        assertNotEquals(DarkVaultColors.surface, LightVaultColors.surface)
        assertNotEquals(DarkVaultColors.textPrimary, LightVaultColors.textPrimary)

        assertEquals(ColorLightSurfaceContainer, LightVaultColors.surfaceHigh)
        assertEquals(ColorLightSurfaceContainerHigh, LightVaultColors.surfaceHighest)
        assertEquals(ColorLightTextMuted, LightVaultColors.textSecondary)
        assertEquals(MidnightBlue, LightVaultColors.textPrimary)
    }

    @Test
    fun givenSecuritySummaryPalette_whenReadingGradientTokens_thenThemeOwnsStatusColors() {
        assertEquals(VaultDanger, DarkVaultColors.securityPoorGradientStart)
        assertEquals(VaultAmber, DarkVaultColors.securityModerateGradientStart)
        assertEquals(VaultGreen, DarkVaultColors.securityExcellentGradientStart)

        assertNotEquals(DarkVaultColors.danger, LightVaultColors.danger)
        assertEquals(VaultDanger, LightVaultColors.securityPoorGradientStart)
        assertEquals(VaultAmber, LightVaultColors.securityModerateGradientStart)
        assertEquals(VaultGreen, LightVaultColors.securityExcellentGradientStart)
    }
}
