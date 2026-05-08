package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

sealed interface SettingsAction {
    data object OnSearchClick : SettingsAction
    data class OnItemClick(val item: SettingsItemKind) : SettingsAction
}
