package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model

data class AppPreferences(
    val userName: String = "",
    val language: AppLanguage = AppLanguage.PortugueseBrazil,
    val themePreference: AppThemePreference = AppThemePreference.Dark,
    val hasCompletedOnboarding: Boolean = false
)
