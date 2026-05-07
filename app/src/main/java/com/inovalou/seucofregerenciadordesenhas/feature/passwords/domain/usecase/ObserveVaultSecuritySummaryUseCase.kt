package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecuritySummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.map

class ObserveVaultSecuritySummaryUseCase @Inject constructor(
    private val repository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase
) {

    operator fun invoke() = repository.observePasswordSecuritySnapshots().map { snapshots ->
        if (snapshots.isEmpty()) {
            return@map VaultSecuritySummary.empty()
        }

        val averageScore = snapshots
            .toSecurityAnalyses(evaluatePasswordSecurityUseCase)
            .map { analysis -> analysis.scorePercent }
            .average()
            .roundToInt()

        VaultSecuritySummary(
            totalPasswords = snapshots.size,
            averageScorePercent = averageScore,
            status = averageScore.toVaultSecurityStatus()
        )
    }
}
