package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.mapper

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary

fun PasswordEntity.toDomain(): PasswordSummary = PasswordSummary(
    id = id,
    title = title,
    login = login,
    iconKey = iconKey
)

fun PasswordSummary.toEntity(): PasswordEntity = PasswordEntity(
    id = id,
    title = title,
    login = login,
    iconKey = iconKey
)
