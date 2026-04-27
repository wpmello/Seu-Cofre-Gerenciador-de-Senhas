package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityStatus
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecuritySummary

internal fun VaultSecuritySummary.toCategoriesSecuritySummaryUiModel(): SecuritySummaryUiModel {
    return SecuritySummaryUiModel(
        statusResId = when (status) {
            VaultSecurityStatus.Poor -> R.string.categories_security_poor
            VaultSecurityStatus.Moderate -> R.string.categories_security_moderate
            VaultSecurityStatus.Good -> R.string.categories_security_good
            VaultSecurityStatus.Excellent -> R.string.categories_security_excellent
        },
        totalItems = totalPasswords,
        visualState = when (status) {
            VaultSecurityStatus.Poor -> SecuritySummaryVisualState.Poor
            VaultSecurityStatus.Moderate -> SecuritySummaryVisualState.Moderate
            VaultSecurityStatus.Good -> SecuritySummaryVisualState.Good
            VaultSecurityStatus.Excellent -> SecuritySummaryVisualState.Excellent
        }
    )
}
