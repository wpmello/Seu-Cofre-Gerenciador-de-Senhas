package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.mapper

import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryEntity
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    itemCount = itemCount
)
