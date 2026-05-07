package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityAnalysis
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityGuidance
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityTag

internal fun PasswordSecurityAnalysis.toEditPasswordSecuritySectionUiState():
    EditPasswordSecuritySectionUiState {
    return EditPasswordSecuritySectionUiState(
        scorePercent = scorePercent,
        visualState = when (riskLevel) {
            PasswordSecurityRiskLevel.High -> EditPasswordSecurityVisualState.HighRisk
            PasswordSecurityRiskLevel.Medium -> EditPasswordSecurityVisualState.MediumRisk
            PasswordSecurityRiskLevel.Low -> EditPasswordSecurityVisualState.Safe
        },
        riskTitleResId = when (riskLevel) {
            PasswordSecurityRiskLevel.High -> R.string.edit_password_security_title
            PasswordSecurityRiskLevel.Medium -> R.string.edit_password_security_moderate_title
            PasswordSecurityRiskLevel.Low -> R.string.edit_password_security_strong_title
        },
        tagResIds = tags.map { tag ->
            when (tag) {
                PasswordSecurityTag.Weak -> R.string.edit_password_security_tag_weak
                PasswordSecurityTag.Duplicate -> R.string.edit_password_security_tag_duplicate
                PasswordSecurityTag.Safe -> R.string.edit_password_security_tag_safe
            }
        },
        alertResId = when (guidance) {
            PasswordSecurityGuidance.HighRisk -> R.string.edit_password_security_alert_high
            PasswordSecurityGuidance.MediumRisk -> R.string.edit_password_security_alert_medium
            PasswordSecurityGuidance.Safe -> R.string.edit_password_security_alert_safe
        }
    )
}
