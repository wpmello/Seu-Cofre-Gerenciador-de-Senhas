package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke() {
        repository.completeOnboarding()
    }
}
