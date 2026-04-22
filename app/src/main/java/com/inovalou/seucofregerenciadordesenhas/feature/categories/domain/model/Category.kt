package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model

data class Category(
    val id: Long,
    val name: String,
    val iconKey: String,
    val itemCount: Int,
    val lastModifiedAt: Long
)
