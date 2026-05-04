package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

class ObservePasswordsUseCase @Inject constructor(
    private val repository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase
) {

    operator fun invoke() = combine(
        repository.observePasswords(),
        repository.observePasswordSecuritySnapshots()
    ) { passwords, securitySnapshots ->
        passwords.withSecurityRiskLevels(
            securitySnapshots.toRiskLevelByPasswordId(evaluatePasswordSecurityUseCase)
        )
    }
}
