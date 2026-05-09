package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class ObserveAppPreferencesUseCase @Inject constructor(
    private val repository: AppPreferencesRepository
) {
    operator fun invoke() = repository.observePreferences()
}
