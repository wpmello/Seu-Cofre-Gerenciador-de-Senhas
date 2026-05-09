package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference

sealed interface SettingsAction {
    data object OnSearchClick : SettingsAction
    data object OnUserCardClick : SettingsAction
    data class OnUserNameDraftChange(val userName: String) : SettingsAction
    data object OnSaveUserNameClick : SettingsAction
    data object OnDismissUserNameEditor : SettingsAction
    data class OnItemClick(val item: SettingsItemKind) : SettingsAction
    data class OnLanguageDraftSelected(val language: AppLanguage) : SettingsAction
    data object OnSaveLanguageClick : SettingsAction
    data object OnDismissLanguageDialog : SettingsAction
    data class OnThemeDraftSelected(val themePreference: AppThemePreference) : SettingsAction
    data object OnSaveThemeClick : SettingsAction
    data object OnDismissThemeDialog : SettingsAction
    data object OnDismissAboutDialog : SettingsAction
}
