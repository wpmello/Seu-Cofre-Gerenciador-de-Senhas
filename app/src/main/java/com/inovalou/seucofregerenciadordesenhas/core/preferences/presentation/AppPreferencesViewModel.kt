package com.inovalou.seucofregerenciadordesenhas.core.preferences.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.ObserveAppPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppPreferencesViewModel @Inject constructor(
    observeAppPreferencesUseCase: ObserveAppPreferencesUseCase
) : ViewModel() {

    val preferences: StateFlow<AppPreferences> = observeAppPreferencesUseCase()
        .catch { emit(AppPreferences()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppPreferences()
        )
}
