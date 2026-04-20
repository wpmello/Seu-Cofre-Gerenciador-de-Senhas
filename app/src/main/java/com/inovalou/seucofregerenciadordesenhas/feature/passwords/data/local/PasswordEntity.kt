package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "login")
    val login: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "encrypted_password")
    val encryptedPassword: String,
    @ColumnInfo(name = "password_iv")
    val passwordIv: String,
    @ColumnInfo(name = "password_cipher_version")
    val passwordCipherVersion: Int,
    @ColumnInfo(name = "icon_key")
    val iconKey: String
)
