package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityAnalysis
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary

internal fun List<PasswordSecuritySnapshot>.toSecurityAnalysisByPasswordId(
    evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase
): Map<Long, PasswordSecurityAnalysis> {
    val analyses = toSecurityAnalyses(evaluatePasswordSecurityUseCase)
    return zip(analyses).associate { (snapshot, analysis) ->
        snapshot.passwordId to analysis
    }
}

internal fun List<PasswordSecuritySnapshot>.toSecurityAnalyses(
    evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase
): List<PasswordSecurityAnalysis> {
    val duplicateCounts = groupingBy { snapshot -> snapshot.fingerprint }.eachCount()
    return map { snapshot ->
        evaluatePasswordSecurityUseCase(
            password = snapshot.password,
            isDuplicate = duplicateCounts.getValue(snapshot.fingerprint) > 1
        )
    }
}

internal fun List<PasswordSecuritySnapshot>.toRiskLevelByPasswordId(
    evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase
): Map<Long, PasswordSecurityRiskLevel> =
    toSecurityAnalysisByPasswordId(evaluatePasswordSecurityUseCase)
        .mapValues { (_, analysis) -> analysis.riskLevel }

internal fun List<PasswordSummary>.withSecurityRiskLevels(
    riskLevelByPasswordId: Map<Long, PasswordSecurityRiskLevel>
): List<PasswordSummary> = map { password ->
    password.copy(
        securityRiskLevel = riskLevelByPasswordId[password.id] ?: PasswordSecurityRiskLevel.High
    )
}
