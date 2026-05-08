package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model

data class PasswordSummary(
    val id: Long,
    val title: String,
    val login: String,
    val categoryId: Long?,
    val categoryName: String?,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val securityRiskLevel: PasswordSecurityRiskLevel = PasswordSecurityRiskLevel.High
)
