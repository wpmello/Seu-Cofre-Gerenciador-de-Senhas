package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

class ObserveRecentPasswordsUseCase @Inject constructor(
    private val repository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase
) {

    operator fun invoke(limit: Int) = combine(
        repository.observeRecentPasswords(limit),
        repository.observePasswordSecuritySnapshots()
    ) { passwords, securitySnapshots ->
        passwords.withSecurityRiskLevels(
            securitySnapshots.toRiskLevelByPasswordId(evaluatePasswordSecurityUseCase)
        )
    }
}
