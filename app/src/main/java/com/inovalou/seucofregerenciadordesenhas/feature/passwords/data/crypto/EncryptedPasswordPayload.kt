package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto

data class EncryptedPasswordPayload(
    val cipherText: String,
    val iv: String,
    val version: Int
)
