package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto

interface PasswordFingerprintGenerator {

    fun generate(password: String): String
}
