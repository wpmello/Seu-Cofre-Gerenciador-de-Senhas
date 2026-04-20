package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject

class GeneratePasswordTitleUseCase @Inject constructor(
    private val passwordRepository: PasswordRepository
) {

    suspend operator fun invoke(rawTitle: String): String {
        val normalizedTitle = rawTitle.trim()
        if (normalizedTitle.isNotBlank()) {
            return normalizedTitle
        }

        return "App ${passwordRepository.getPasswordCount() + 1}"
    }
}
