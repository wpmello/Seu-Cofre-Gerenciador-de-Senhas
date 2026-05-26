package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityDetailsItem
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class ObserveVaultSecurityDetailsUseCase @Inject constructor(
    private val repository: PasswordRepository,
    private val evaluatePasswordSecurityUseCase: EvaluatePasswordSecurityUseCase,
    private val dispatchers: AppDispatchers
) {

    operator fun invoke() = combine(
        repository.observePasswords(),
        repository.observePasswordSecuritySnapshots()
    ) { passwords, securitySnapshots ->
        passwords.toVaultSecurityDetails(securitySnapshots)
    }

    private suspend fun List<PasswordSummary>.toVaultSecurityDetails(
        securitySnapshots: List<PasswordSecuritySnapshot>
    ): VaultSecurityDetails {
        if (isEmpty() || securitySnapshots.isEmpty()) {
            return VaultSecurityDetails.empty()
        }

        val passwordById = associateBy { password -> password.id }
        val analysisByPasswordId = withContext(dispatchers.default) {
            securitySnapshots.toSecurityAnalysisByPasswordId(
                evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase
            )
        }

        val items = securitySnapshots.mapNotNull { snapshot ->
            val password = passwordById[snapshot.passwordId] ?: return@mapNotNull null
            val analysis = analysisByPasswordId[snapshot.passwordId] ?: return@mapNotNull null
            PasswordSecurityDetailsItem(
                id = password.id,
                title = password.title,
                login = password.login,
                categoryName = password.categoryName,
                scorePercent = analysis.scorePercent,
                bucket = analysis.scorePercent.toPasswordSecurityBucket(),
                tags = analysis.tags
            )
        }.sortedWith(
            compareBy<PasswordSecurityDetailsItem>({ it.scorePercent }, { it.title.lowercase() })
        )

        if (items.isEmpty()) {
            return VaultSecurityDetails.empty()
        }

        val averageScorePercent = items
            .map { item -> item.scorePercent }
            .average()
            .roundToInt()

        val groupedPasswords = PasswordSecurityBucket.entries.associateWith { bucket ->
            items.filter { item -> item.bucket == bucket }
        }

        return VaultSecurityDetails(
            totalPasswords = items.size,
            averageScorePercent = averageScorePercent,
            status = averageScorePercent.toVaultSecurityStatus(),
            passwordsByBucket = groupedPasswords
        )
    }
}
