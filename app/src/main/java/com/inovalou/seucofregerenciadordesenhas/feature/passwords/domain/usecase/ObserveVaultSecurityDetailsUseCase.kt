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
        createVaultSecurityDetailsOnDefaultDispatcher(
            passwords = passwords,
            securitySnapshots = securitySnapshots
        )
    }

    private suspend fun createVaultSecurityDetailsOnDefaultDispatcher(
        passwords: List<PasswordSummary>,
        securitySnapshots: List<PasswordSecuritySnapshot>
    ): VaultSecurityDetails = withContext(dispatchers.default) {
        if (passwords.isEmpty() || securitySnapshots.isEmpty()) {
            return@withContext VaultSecurityDetails.empty()
        }

        val passwordById = passwords.associateBy { password -> password.id }
        val analysisByPasswordId = securitySnapshots.toSecurityAnalysisByPasswordId(
            evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase
        )

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
            return@withContext VaultSecurityDetails.empty()
        }

        val averageScorePercent = items
            .map { item -> item.scorePercent }
            .average()
            .roundToInt()

        val groupedPasswords = PasswordSecurityBucket.entries.associateWith { bucket ->
            items.filter { item -> item.bucket == bucket }
        }

        VaultSecurityDetails(
            totalPasswords = items.size,
            averageScorePercent = averageScorePercent,
            status = averageScorePercent.toVaultSecurityStatus(),
            passwordsByBucket = groupedPasswords
        )
    }
}
