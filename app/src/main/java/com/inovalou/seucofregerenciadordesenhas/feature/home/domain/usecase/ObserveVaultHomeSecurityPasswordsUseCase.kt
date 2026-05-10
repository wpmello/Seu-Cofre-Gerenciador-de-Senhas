package com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHomeSecurityPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityDetailsItem
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecurityDetailsUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class ObserveVaultHomeSecurityPasswordsUseCase @Inject constructor(
    private val observeVaultSecurityDetailsUseCase: ObserveVaultSecurityDetailsUseCase
) {

    operator fun invoke(bucket: PasswordSecurityBucket) = observeVaultSecurityDetailsUseCase()
        .map { details ->
            details.passwordsFor(bucket).map { password -> password.toHomeSecurityPassword() }
        }
}

private fun PasswordSecurityDetailsItem.toHomeSecurityPassword(): VaultHomeSecurityPassword =
    VaultHomeSecurityPassword(
        id = id,
        title = title,
        login = login,
        bucket = bucket,
        scorePercent = scorePercent
    )
