package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.repository

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto.EncryptedPasswordPayload
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto.PasswordCipher
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto.PasswordFingerprintGenerator
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordsLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.mapper.toDetailsDomain
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.mapper.toDomain
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSearchResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PasswordRepositoryImpl @Inject constructor(
    private val localDataSource: PasswordsLocalDataSource,
    private val passwordCipher: PasswordCipher,
    private val passwordFingerprintGenerator: PasswordFingerprintGenerator,
    private val timeProvider: TimeProvider,
    private val dispatchers: AppDispatchers
) : PasswordRepository {

    override fun observePasswords(): Flow<List<PasswordSummary>> =
        localDataSource.observePasswords().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }

    override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
        localDataSource.observePasswordsByCategoryId(categoryId).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }

    override fun observePasswordCount(): Flow<Int> = localDataSource.observePasswordCount()

    override fun observeRecentPasswords(limit: Int): Flow<List<PasswordSummary>> =
        localDataSource.observeRecentPasswords(limit).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }

    override fun observePasswordSearchResults(query: String): Flow<List<PasswordSearchResult>> =
        localDataSource.observePasswordSearchResults(query).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }

    override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
        localDataSource.observePasswords().map { entities ->
            createSecuritySnapshots(entities)
        }

    override suspend fun getPasswordCount(): Int = localDataSource.getPasswordCount()

    override suspend fun createPassword(password: NewPassword): Long {
        val passwordMaterial = createPersistablePasswordMaterial(password.password)
        val persistedAt = password.createdAt.takeIf { it > 0L } ?: timeProvider.currentTimeMillis()
        val updatedAt = password.updatedAt.takeIf { it > 0L } ?: persistedAt

        return localDataSource.createPassword(
            PasswordEntity(
                title = password.title,
                login = password.login,
                category = password.categoryName.orEmpty(),
                categoryId = password.categoryId,
                encryptedPassword = passwordMaterial.encryptedPassword.cipherText,
                passwordIv = passwordMaterial.encryptedPassword.iv,
                passwordCipherVersion = passwordMaterial.encryptedPassword.version,
                iconKey = "",
                note = password.note,
                passwordFingerprint = passwordMaterial.fingerprint,
                createdAt = persistedAt,
                updatedAt = updatedAt
            )
        )
    }

    override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? {
        val entity = localDataSource.getPasswordById(passwordId) ?: return null
        val passwordMaterial = createDetailsPasswordMaterial(entity)
        passwordMaterial.missingFingerprint?.let { fingerprint ->
            localDataSource.updatePasswordFingerprint(
                passwordId = entity.id,
                passwordFingerprint = fingerprint
            )
        }
        return entity.toDetailsDomain(password = passwordMaterial.plainPassword)
    }

    override suspend fun updatePassword(password: PasswordDetails) {
        val passwordMaterial = createPersistablePasswordMaterial(password.password)
        localDataSource.updatePassword(
            PasswordEntity(
                id = password.id,
                title = password.title,
                login = password.login,
                category = password.categoryName.orEmpty(),
                categoryId = password.categoryId,
                encryptedPassword = passwordMaterial.encryptedPassword.cipherText,
                passwordIv = passwordMaterial.encryptedPassword.iv,
                passwordCipherVersion = passwordMaterial.encryptedPassword.version,
                iconKey = password.iconKey,
                note = password.note,
                passwordFingerprint = passwordMaterial.fingerprint,
                createdAt = password.createdAt,
                updatedAt = password.updatedAt
            )
        )
    }

    override suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long?): Boolean {
        backfillMissingFingerprints()
        val fingerprint = generatePasswordFingerprint(password)
        return localDataSource.countPasswordsWithFingerprint(
            passwordFingerprint = fingerprint,
            excludePasswordId = excludePasswordId
        ) > 0
    }

    override suspend fun deletePasswordById(passwordId: Long): Boolean =
        localDataSource.deletePasswordById(passwordId)

    private suspend fun backfillMissingFingerprints() {
        localDataSource.getPasswordsMissingFingerprint().forEach { entity ->
            val fingerprint = createMissingFingerprint(entity)
            localDataSource.updatePasswordFingerprint(
                passwordId = entity.id,
                passwordFingerprint = fingerprint
            )
        }
    }

    private suspend fun createSecuritySnapshots(
        entities: List<PasswordEntity>
    ): List<PasswordSecuritySnapshot> =
        withContext(dispatchers.default) {
            entities.map { entity ->
                val plainPassword = entity.decryptPlainPassword()
                PasswordSecuritySnapshot(
                    passwordId = entity.id,
                    password = plainPassword,
                    fingerprint = entity.passwordFingerprint
                        ?.takeIf { it.isNotBlank() }
                        ?: passwordFingerprintGenerator.generate(plainPassword)
                )
            }
        }

    private suspend fun createPersistablePasswordMaterial(password: String): PersistablePasswordMaterial =
        withContext(dispatchers.default) {
            PersistablePasswordMaterial(
                encryptedPassword = passwordCipher.encrypt(password),
                fingerprint = passwordFingerprintGenerator.generate(password)
            )
        }

    private suspend fun createDetailsPasswordMaterial(entity: PasswordEntity): DetailsPasswordMaterial =
        withContext(dispatchers.default) {
            val plainPassword = entity.decryptPlainPassword()
            DetailsPasswordMaterial(
                plainPassword = plainPassword,
                missingFingerprint = if (entity.passwordFingerprint.isNullOrBlank()) {
                    passwordFingerprintGenerator.generate(plainPassword)
                } else {
                    null
                }
            )
        }

    private suspend fun createMissingFingerprint(entity: PasswordEntity): String =
        withContext(dispatchers.default) {
            passwordFingerprintGenerator.generate(entity.decryptPlainPassword())
        }

    private suspend fun generatePasswordFingerprint(password: String): String =
        withContext(dispatchers.default) {
            passwordFingerprintGenerator.generate(password)
        }

    private fun PasswordEntity.decryptPlainPassword(): String =
        passwordCipher.decrypt(
            cipherText = encryptedPassword,
            iv = passwordIv,
            version = passwordCipherVersion
        )

    private data class PersistablePasswordMaterial(
        val encryptedPassword: EncryptedPasswordPayload,
        val fingerprint: String
    )

    private data class DetailsPasswordMaterial(
        val plainPassword: String,
        val missingFingerprint: String?
    )
}
