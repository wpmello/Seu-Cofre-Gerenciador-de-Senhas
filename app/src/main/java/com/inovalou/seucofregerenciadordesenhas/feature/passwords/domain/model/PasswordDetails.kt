package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model

data class PasswordDetails(
    val id: Long,
    val title: String,
    val login: String,
    val password: String,
    val categoryId: Long?,
    val categoryName: String?,
    val iconKey: String,
    val createdAt: Long,
    val updatedAt: Long,
    val note: String? = null
)
