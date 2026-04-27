package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityStatus
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

        val duplicateCounts = snapshots.groupingBy { it.fingerprint }.eachCount()
        val averageScore = snapshots
            .map { snapshot ->
                evaluatePasswordSecurityUseCase(
                    password = snapshot.password,
                    isDuplicate = duplicateCounts.getValue(snapshot.fingerprint) > 1
                ).scorePercent
            }
            .average()
            .roundToInt()

        VaultSecuritySummary(
            totalPasswords = snapshots.size,
            averageScorePercent = averageScore,
            status = when (averageScore) {
                in 0..25 -> VaultSecurityStatus.Poor
                in 26..50 -> VaultSecurityStatus.Moderate
                in 51..75 -> VaultSecurityStatus.Good
                else -> VaultSecurityStatus.Excellent
            }
        )
    }
}
