package com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel

data class VaultHome(
    val totalPasswords: Int,
    val weakPasswords: Int,
    val categories: List<VaultHomeCategory>,
    val showOtherCategories: Boolean,
    val recentPasswords: List<VaultHomePassword>
) {
    companion object {
        fun empty(): VaultHome = VaultHome(
            totalPasswords = 0,
            weakPasswords = 0,
            categories = emptyList(),
            showOtherCategories = false,
            recentPasswords = emptyList()
        )
    }
}

data class VaultHomeCategory(
    val id: Long,
    val name: String,
    val iconKey: String,
    val itemCount: Int,
    val lastModifiedAt: Long
)

data class VaultHomePassword(
    val id: Long,
    val title: String,
    val login: String,
    val securityRiskLevel: PasswordSecurityRiskLevel,
    val createdAt: Long,
    val updatedAt: Long
)
