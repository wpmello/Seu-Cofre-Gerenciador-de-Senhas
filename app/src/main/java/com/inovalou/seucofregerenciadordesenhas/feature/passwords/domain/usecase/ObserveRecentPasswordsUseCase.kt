package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class ObserveRecentPasswordsUseCase @Inject constructor(
    private val repository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase,
    private val dispatchers: AppDispatchers
) {

    operator fun invoke(limit: Int) = combine(
        repository.observeRecentPasswords(limit),
        repository.observePasswordSecuritySnapshots()
    ) { passwords, securitySnapshots ->
        val riskLevelByPasswordId = withContext(dispatchers.default) {
            securitySnapshots.toRiskLevelByPasswordId(evaluatePasswordSecurityUseCase)
        }
        passwords.withSecurityRiskLevels(riskLevelByPasswordId)
    }
}
