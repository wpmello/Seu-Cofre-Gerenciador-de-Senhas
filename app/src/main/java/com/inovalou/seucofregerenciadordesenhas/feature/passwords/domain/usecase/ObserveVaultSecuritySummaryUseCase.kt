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
        snapshots.toVaultSecuritySummaryOnDefaultDispatcher()
    }

    private suspend fun List<PasswordSecuritySnapshot>.toVaultSecuritySummaryOnDefaultDispatcher() =
        withContext(dispatchers.default) {
            if (isEmpty()) {
                return@withContext VaultSecuritySummary.empty()
            }

            val averageScore = toSecurityAnalyses(evaluatePasswordSecurityUseCase)
                .map { analysis -> analysis.scorePercent }
                .average()
                .roundToInt()

            VaultSecuritySummary(
                totalPasswords = size,
                averageScorePercent = averageScore,
                status = averageScore.toVaultSecurityStatus()
            )
        }
}
