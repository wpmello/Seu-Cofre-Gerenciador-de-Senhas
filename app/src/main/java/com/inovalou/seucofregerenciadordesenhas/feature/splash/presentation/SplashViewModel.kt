package com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.ObserveAppPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SplashViewModel @Inject constructor(
    observeAppPreferencesUseCase: ObserveAppPreferencesUseCase
) : ViewModel() {

    val uiState = observeAppPreferencesUseCase()
        .catch { emit(AppPreferences()) }
        .combine(minimumSplashDelayFlow()) { preferences, hasWaitedMinimumDuration ->
            SplashLaunchUiState(
                destination = if (hasWaitedMinimumDuration) {
                    if (preferences.hasCompletedOnboarding) {
                        SplashLaunchDestination.App
                    } else {
                        SplashLaunchDestination.Onboarding
                    }
                } else {
                    null
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SplashLaunchUiState()
        )

    private fun minimumSplashDelayFlow() = flow {
        emit(false)
        delay(SplashScreenSpec.animationDurationMillis.toLong())
        emit(true)
    }
}
