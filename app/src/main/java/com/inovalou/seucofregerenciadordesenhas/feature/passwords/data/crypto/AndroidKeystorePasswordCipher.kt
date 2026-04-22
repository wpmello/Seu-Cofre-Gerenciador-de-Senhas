package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidKeystorePasswordCipher @Inject constructor() : PasswordCipher {

    override fun encrypt(plainText: String): EncryptedPasswordPayload {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        return EncryptedPasswordPayload(
            cipherText = Base64.getEncoder().encodeToString(
                cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            ),
            iv = Base64.getEncoder().encodeToString(cipher.iv),
            version = CURRENT_VERSION
        )
    }

    override fun decrypt(cipherText: String, iv: String, version: Int): String {
        require(version == CURRENT_VERSION) {
            "Unsupported password cipher version: $version"
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            javax.crypto.spec.GCMParameterSpec(
                GCM_TAG_LENGTH_BITS,
                Base64.getDecoder().decode(iv)
            )
        )

        return try {
            String(
                cipher.doFinal(Base64.getDecoder().decode(cipherText)),
                StandardCharsets.UTF_8
            )
        } catch (error: AEADBadTagException) {
            throw IllegalStateException("Unable to decrypt stored password", error)
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "seu_cofre_password_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val CURRENT_VERSION = 1
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
