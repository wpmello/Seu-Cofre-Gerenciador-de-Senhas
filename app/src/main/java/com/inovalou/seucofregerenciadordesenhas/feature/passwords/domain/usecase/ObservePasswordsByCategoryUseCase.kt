package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject

class ObservePasswordsByCategoryUseCase @Inject constructor(
    private val repository: PasswordRepository
) {

    operator fun invoke(categoryId: Long) = repository.observePasswordsByCategoryId(categoryId)
}
