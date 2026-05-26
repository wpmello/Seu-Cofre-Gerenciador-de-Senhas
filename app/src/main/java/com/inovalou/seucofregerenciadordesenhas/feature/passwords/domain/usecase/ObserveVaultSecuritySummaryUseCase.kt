package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecuritySummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ObserveVaultSecuritySummaryUseCase @Inject constructor(
    private val repository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase,
    private val dispatchers: AppDispatchers
) {

    operator fun invoke() = repository.observePasswordSecuritySnapshots().map { snapshots ->
        if (snapshots.isEmpty()) {
            return@map VaultSecuritySummary.empty()
        }

        val averageScore = snapshots
            .toSecurityAnalysesOnDefaultDispatcher()
            .map { analysis -> analysis.scorePercent }
            .average()
            .roundToInt()

        VaultSecuritySummary(
            totalPasswords = snapshots.size,
            averageScorePercent = averageScore,
            status = averageScore.toVaultSecurityStatus()
        )
    }

    private suspend fun List<PasswordSecuritySnapshot>.toSecurityAnalysesOnDefaultDispatcher() =
        withContext(dispatchers.default) {
            toSecurityAnalyses(evaluatePasswordSecurityUseCase)
        }
}
