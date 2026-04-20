package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject

class ObservePasswordsUseCase @Inject constructor(
    private val repository: PasswordRepository
) {

    operator fun invoke() = repository.observePasswords()
}
