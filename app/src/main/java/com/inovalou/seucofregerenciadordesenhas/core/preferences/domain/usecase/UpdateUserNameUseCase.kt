package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class UpdateUserNameUseCase @Inject constructor(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(userName: String) {
        repository.updateUserName(userName.trim())
    }
}
