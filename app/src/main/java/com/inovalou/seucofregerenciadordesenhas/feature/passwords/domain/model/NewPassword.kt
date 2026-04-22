package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model

data class NewPassword(
    val title: String,
    val login: String,
    val categoryId: Long?,
    val categoryName: String?,
    val password: String
)
