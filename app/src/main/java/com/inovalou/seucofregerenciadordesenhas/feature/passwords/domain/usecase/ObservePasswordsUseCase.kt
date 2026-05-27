package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class ObservePasswordsUseCase @Inject constructor(
    private val repository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase,
    private val dispatchers: AppDispatchers
) {

    operator fun invoke() = combine(
        repository.observePasswords(),
        repository.observePasswordSecuritySnapshots()
    ) { passwords, securitySnapshots ->
        passwords.withSecurityRiskLevelsOnDefaultDispatcher(securitySnapshots)
    }

    private suspend fun List<PasswordSummary>.withSecurityRiskLevelsOnDefaultDispatcher(
        securitySnapshots: List<PasswordSecuritySnapshot>
    ): List<PasswordSummary> = withContext(dispatchers.default) {
        withSecurityRiskLevels(
            securitySnapshots.toRiskLevelByPasswordId(evaluatePasswordSecurityUseCase)
        )
    }
}
