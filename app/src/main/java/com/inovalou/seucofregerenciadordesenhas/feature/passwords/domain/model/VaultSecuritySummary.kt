package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model

data class VaultSecuritySummary(
    val totalPasswords: Int,
    val averageScorePercent: Int,
    val status: VaultSecurityStatus
) {
    companion object {
        fun empty(): VaultSecuritySummary = VaultSecuritySummary(
            totalPasswords = 0,
            averageScorePercent = 100,
            status = VaultSecurityStatus.Excellent
        )
    }
}

data class PasswordSecuritySnapshot(
    val passwordId: Long = 0L,
    val password: String,
    val fingerprint: String
)

enum class VaultSecurityStatus {
    Poor,
    Moderate,
    Excellent
}
