package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityAnalysis
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject

class AnalyzePasswordSecurityUseCase @Inject constructor(
    private val passwordRepository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase
) {

    suspend operator fun invoke(
        password: String,
        currentPasswordId: Long? = null
    ): PasswordSecurityAnalysis {
        val isDuplicate = password.isNotBlank() && passwordRepository.hasPasswordDuplicate(
            password = password,
            excludePasswordId = currentPasswordId
        )
        return evaluatePasswordSecurityUseCase(
            password = password,
            isDuplicate = isDuplicate
        )
    }
}
