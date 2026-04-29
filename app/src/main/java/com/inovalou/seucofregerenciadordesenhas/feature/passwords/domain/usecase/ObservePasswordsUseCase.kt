package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
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
        val riskLevelByPasswordId = securitySnapshots.toRiskLevelByPasswordId()
        passwords.map { password ->
            password.copy(
                securityRiskLevel = riskLevelByPasswordId[password.id] ?: PasswordSecurityRiskLevel.High
            )
        }
    }

    private fun List<PasswordSecuritySnapshot>.toRiskLevelByPasswordId(): Map<Long, PasswordSecurityRiskLevel> {
        val duplicateCounts = groupingBy { it.fingerprint }.eachCount()
        return associate { snapshot ->
            val analysis = evaluatePasswordSecurityUseCase(
                password = snapshot.password,
                isDuplicate = duplicateCounts.getValue(snapshot.fingerprint) > 1
            )
            snapshot.passwordId to analysis.riskLevel
        }
    }
}
