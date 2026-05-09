package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class UpdateAppLanguageUseCase @Inject constructor(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(language: AppLanguage) {
        repository.updateLanguage(language)
    }
}
