package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState.content())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnSearchClick,
            is SettingsAction.OnItemClick -> Unit
        }
    }
}
