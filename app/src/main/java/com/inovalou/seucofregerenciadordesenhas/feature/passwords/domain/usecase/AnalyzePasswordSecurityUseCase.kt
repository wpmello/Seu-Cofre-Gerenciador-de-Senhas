package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityAnalysis
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.withContext

class AnalyzePasswordSecurityUseCase @Inject constructor(
    private val passwordRepository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase,
    private val dispatchers: AppDispatchers
) {

    suspend operator fun invoke(
        password: String,
        currentPasswordId: Long? = null
    ): PasswordSecurityAnalysis {
        val isDuplicate = password.isNotBlank() && passwordRepository.hasPasswordDuplicate(
            password = password,
            excludePasswordId = currentPasswordId
        )
        return withContext(dispatchers.default) {
            evaluatePasswordSecurityUseCase(
                password = password,
                isDuplicate = isDuplicate
            )
        }
    }
}
