package com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation

sealed interface OnboardingAction {
    data object OnNextClick : OnboardingAction
}
