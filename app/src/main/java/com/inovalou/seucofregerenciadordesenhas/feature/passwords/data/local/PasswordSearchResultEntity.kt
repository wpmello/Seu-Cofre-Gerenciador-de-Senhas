package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import androidx.room.ColumnInfo

data class PasswordSearchResultEntity(
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "icon_key")
    val iconKey: String
)
