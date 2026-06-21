package com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isCompleting: Boolean = false
) {
    val isLastStep: Boolean = currentStep == LAST_STEP_INDEX

    companion object {
        const val STEP_COUNT = 3
        const val LAST_STEP_INDEX = STEP_COUNT - 1
    }
}
