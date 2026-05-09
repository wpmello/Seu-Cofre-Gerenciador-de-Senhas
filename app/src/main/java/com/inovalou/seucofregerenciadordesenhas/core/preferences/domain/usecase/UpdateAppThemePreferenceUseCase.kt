package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class UpdateAppThemePreferenceUseCase @Inject constructor(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(themePreference: AppThemePreference) {
        repository.updateThemePreference(themePreference)
    }
}
