package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityStatus

internal fun Int.toVaultSecurityStatus(): VaultSecurityStatus = when (coerceIn(0, 100)) {
    in 0..49 -> VaultSecurityStatus.Poor
    in 50..90 -> VaultSecurityStatus.Moderate
    else -> VaultSecurityStatus.Excellent
}

internal fun Int.toPasswordSecurityBucket(): PasswordSecurityBucket = when (toVaultSecurityStatus()) {
    VaultSecurityStatus.Poor -> PasswordSecurityBucket.Weak
    VaultSecurityStatus.Moderate -> PasswordSecurityBucket.Moderate
    VaultSecurityStatus.Excellent -> PasswordSecurityBucket.Safe
}
