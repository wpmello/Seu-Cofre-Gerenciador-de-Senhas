package com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingEffect> = _effects.receiveAsFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            OnboardingAction.OnNextClick -> advance()
        }
    }

    private fun advance() {
        val currentState = _uiState.value
        if (currentState.isCompleting) {
            return
        }
        if (!currentState.isLastStep) {
            _uiState.update { state -> state.copy(currentStep = state.currentStep + 1) }
            return
        }

        _uiState.update { state -> state.copy(isCompleting = true) }
        viewModelScope.launch {
            try {
                completeOnboardingUseCase()
                _effects.send(OnboardingEffect.NavigateToApp)
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    throw exception
                }
                _uiState.update { state -> state.copy(isCompleting = false) }
            }
        }
    }
}

sealed interface OnboardingEffect {
    data object NavigateToApp : OnboardingEffect
}
