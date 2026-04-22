package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto

interface PasswordCipher {

    fun encrypt(plainText: String): EncryptedPasswordPayload
}
