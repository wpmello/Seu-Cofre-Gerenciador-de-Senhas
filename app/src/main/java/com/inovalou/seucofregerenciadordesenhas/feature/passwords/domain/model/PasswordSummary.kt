package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model

data class PasswordSummary(
    val id: Long,
    val title: String,
    val login: String,
    val category: String
)
