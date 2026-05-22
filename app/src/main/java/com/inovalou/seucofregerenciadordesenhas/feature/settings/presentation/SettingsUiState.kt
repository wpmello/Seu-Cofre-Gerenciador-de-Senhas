package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference

data class SettingsUiState(
    val user: SettingsUserUiModel = SettingsUserUiModel(),
    val items: List<SettingsItemUiModel> = emptyList(),
    val selectedLanguage: AppLanguage = AppLanguage.PortugueseBrazil,
    val selectedTheme: AppThemePreference = AppThemePreference.Dark,
    val nameEditor: SettingsNameEditorUiState? = null,
    val languageDialog: SettingsLanguageDialogUiState? = null,
    val themeDialog: SettingsThemeDialogUiState? = null,
    val aboutDialogVisible: Boolean = false,
    val contentState: SettingsContentState = SettingsContentState.Loading
) {
    companion object {
        fun content(
            preferences: AppPreferences = AppPreferences(),
            transientState: SettingsTransientUiState = SettingsTransientUiState()
        ): SettingsUiState = SettingsUiState(
            user = SettingsUserUiModel(
                name = preferences.userName.takeIf { userName -> userName.isNotBlank() },
                fallbackNameResId = R.string.settings_user_name_fallback,
                encryptedStatusResId = R.string.vault_encrypted_indicator
            ),
            items = settingsItems(preferences),
            selectedLanguage = preferences.language,
            selectedTheme = preferences.themePreference,
            nameEditor = transientState.nameEditor,
            languageDialog = transientState.languageDialog,
            themeDialog = transientState.themeDialog,
            aboutDialogVisible = transientState.aboutDialogVisible,
            contentState = SettingsContentState.Content
        )

        private fun settingsItems(preferences: AppPreferences): List<SettingsItemUiModel> = listOf(
            SettingsItemUiModel(
                kind = SettingsItemKind.Language,
                titleResId = R.string.settings_language_title,
                subtitleResId = R.string.settings_language_subtitle,
                trailingLabelResId = preferences.language.labelResId(),
                icon = SettingsItemIcon.Password
            ),
            SettingsItemUiModel(
                kind = SettingsItemKind.Theme,
                titleResId = R.string.settings_theme_title,
                subtitleResId = R.string.settings_theme_subtitle,
                trailingLabelResId = preferences.themePreference.labelResId(),
                icon = SettingsItemIcon.Palette
            ),
            SettingsItemUiModel(
                kind = SettingsItemKind.About,
                titleResId = R.string.settings_about_title,
                subtitleResId = R.string.settings_about_subtitle,
                icon = SettingsItemIcon.Info
            )
        )
    }
}

data class SettingsUserUiModel(
    val name: String? = null,
    @StringRes val fallbackNameResId: Int = R.string.settings_user_name_fallback,
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

data class SettingsTransientUiState(
    val nameEditor: SettingsNameEditorUiState? = null,
    val languageDialog: SettingsLanguageDialogUiState? = null,
    val themeDialog: SettingsThemeDialogUiState? = null,
    val aboutDialogVisible: Boolean = false
)

data class SettingsNameEditorUiState(
    val draftName: String,
    val isSaving: Boolean = false
)

data class SettingsLanguageDialogUiState(
    val selectedLanguage: AppLanguage,
    val draftLanguage: AppLanguage,
    val isSaving: Boolean = false
)

data class SettingsThemeDialogUiState(
    val selectedTheme: AppThemePreference,
    val draftTheme: AppThemePreference,
    val isSaving: Boolean = false
)

@StringRes
fun AppLanguage.labelResId(): Int = when (this) {
    AppLanguage.PortugueseBrazil -> R.string.settings_language_portuguese
    AppLanguage.English -> R.string.settings_language_english
    AppLanguage.Spanish -> R.string.settings_language_spanish
}

@StringRes
fun AppThemePreference.labelResId(): Int = when (this) {
    AppThemePreference.Light -> R.string.settings_theme_current_light
    AppThemePreference.Dark -> R.string.settings_theme_current_dark
    AppThemePreference.System -> R.string.settings_theme_current_system
}
