package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.R

data class SettingsUiState(
    val user: SettingsUserUiModel = SettingsUserUiModel(),
    val items: List<SettingsItemUiModel> = emptyList(),
    val contentState: SettingsContentState = SettingsContentState.Loading
) {
    companion object {
        fun content(): SettingsUiState = SettingsUiState(
            user = SettingsUserUiModel(
                name = "Alex Thompson",
                encryptedStatusResId = R.string.vault_encrypted_indicator
            ),
            items = listOf(
                SettingsItemUiModel(
                    kind = SettingsItemKind.Language,
                    titleResId = R.string.settings_language_title,
                    subtitleResId = R.string.settings_language_subtitle,
                    icon = SettingsItemIcon.Password
                ),
                SettingsItemUiModel(
                    kind = SettingsItemKind.Theme,
                    titleResId = R.string.settings_theme_title,
                    subtitleResId = R.string.settings_theme_subtitle,
                    trailingLabelResId = R.string.settings_theme_current_dark,
                    icon = SettingsItemIcon.Palette
                ),
                SettingsItemUiModel(
                    kind = SettingsItemKind.About,
                    titleResId = R.string.settings_about_title,
                    subtitleResId = R.string.settings_about_subtitle,
                    icon = SettingsItemIcon.Info
                )
            ),
            contentState = SettingsContentState.Content
        )
    }
}

data class SettingsUserUiModel(
    val name: String = "",
    @StringRes val encryptedStatusResId: Int = R.string.vault_encrypted_indicator
)

data class SettingsItemUiModel(
    val kind: SettingsItemKind,
    @StringRes val titleResId: Int,
    @StringRes val subtitleResId: Int,
    @StringRes val trailingLabelResId: Int? = null,
    val icon: SettingsItemIcon
)

enum class SettingsItemKind {
    Language,
    Theme,
    About
}

enum class SettingsItemIcon {
    Password,
    Palette,
    Info
}

sealed interface SettingsContentState {
    data object Loading : SettingsContentState
    data object Content : SettingsContentState
    data object Empty : SettingsContentState
    data class Error(@StringRes val messageResId: Int = R.string.settings_load_error) : SettingsContentState
}
