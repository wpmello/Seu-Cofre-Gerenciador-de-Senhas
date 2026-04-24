package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model

data class PasswordSecurityAnalysis(
    val scorePercent: Int,
    val riskLevel: PasswordSecurityRiskLevel,
    val tags: List<PasswordSecurityTag>,
    val guidance: PasswordSecurityGuidance
)

enum class PasswordSecurityRiskLevel {
    High,
    Medium,
    Low
}

enum class PasswordSecurityTag {
    Weak,
    Duplicate,
    Safe
}

enum class PasswordSecurityGuidance {
    HighRisk,
    MediumRisk,
    Safe
}
