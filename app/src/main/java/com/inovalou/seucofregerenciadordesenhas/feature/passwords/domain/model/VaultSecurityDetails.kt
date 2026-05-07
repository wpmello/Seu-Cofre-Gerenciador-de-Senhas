package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model

data class VaultSecurityDetails(
    val totalPasswords: Int,
    val averageScorePercent: Int,
    val status: VaultSecurityStatus,
    val passwordsByBucket: Map<PasswordSecurityBucket, List<PasswordSecurityDetailsItem>>
) {
    fun countFor(bucket: PasswordSecurityBucket): Int = passwordsFor(bucket).size

    fun passwordsFor(bucket: PasswordSecurityBucket): List<PasswordSecurityDetailsItem> =
        passwordsByBucket[bucket].orEmpty()

    companion object {
        fun empty(): VaultSecurityDetails = VaultSecurityDetails(
            totalPasswords = 0,
            averageScorePercent = 100,
            status = VaultSecurityStatus.Excellent,
            passwordsByBucket = PasswordSecurityBucket.entries.associateWith { emptyList() }
        )
    }
}

data class PasswordSecurityDetailsItem(
    val id: Long,
    val title: String,
    val login: String,
    val categoryName: String?,
    val scorePercent: Int,
    val bucket: PasswordSecurityBucket,
    val tags: List<PasswordSecurityTag>
)

enum class PasswordSecurityBucket {
    Weak,
    Moderate,
    Safe
}
