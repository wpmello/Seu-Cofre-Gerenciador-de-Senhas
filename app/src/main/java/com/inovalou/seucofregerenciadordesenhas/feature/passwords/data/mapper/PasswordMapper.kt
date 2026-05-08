package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.mapper

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary

fun PasswordEntity.toDomain(): PasswordSummary = PasswordSummary(
    id = id,
    title = title,
    login = login,
    categoryId = categoryId,
    categoryName = category.ifBlank { null },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PasswordEntity.toDetailsDomain(password: String): PasswordDetails = PasswordDetails(
    id = id,
    title = title,
    login = login,
    password = password,
    note = note,
    categoryId = categoryId,
    categoryName = category.ifBlank { null },
    iconKey = iconKey,
    createdAt = createdAt,
    updatedAt = updatedAt
)
