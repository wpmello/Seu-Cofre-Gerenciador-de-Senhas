package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary

internal fun List<PasswordSecuritySnapshot>.toRiskLevelByPasswordId(
    evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase
): Map<Long, PasswordSecurityRiskLevel> {
    val duplicateCounts = groupingBy { snapshot -> snapshot.fingerprint }.eachCount()
    return associate { snapshot ->
        val analysis = evaluatePasswordSecurityUseCase(
            password = snapshot.password,
            isDuplicate = duplicateCounts.getValue(snapshot.fingerprint) > 1
        )
        snapshot.passwordId to analysis.riskLevel
    }
}

internal fun List<PasswordSummary>.withSecurityRiskLevels(
    riskLevelByPasswordId: Map<Long, PasswordSecurityRiskLevel>
): List<PasswordSummary> = map { password ->
    password.copy(
        securityRiskLevel = riskLevelByPasswordId[password.id] ?: PasswordSecurityRiskLevel.High
    )
}
