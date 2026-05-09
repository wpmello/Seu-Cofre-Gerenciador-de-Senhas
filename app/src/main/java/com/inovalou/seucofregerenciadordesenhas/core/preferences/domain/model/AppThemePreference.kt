package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model

enum class AppThemePreference(val storageValue: String) {
    Light(storageValue = "light"),
    Dark(storageValue = "dark"),
    System(storageValue = "system");

    companion object {
        fun fromStorageValue(value: String?): AppThemePreference =
            entries.firstOrNull { themePreference -> themePreference.storageValue == value } ?: Dark
    }
}
