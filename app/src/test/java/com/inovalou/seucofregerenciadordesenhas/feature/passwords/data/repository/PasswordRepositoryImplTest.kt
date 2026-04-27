package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.repository

import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto.EncryptedPasswordPayload
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto.PasswordCipher
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto.PasswordFingerprintGenerator
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordsLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordRepositoryImplTest {

    @Test
    fun givenLocalEntities_whenObservingPasswords_thenMapsEntitiesToDomainModels() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(
            initialPasswords = listOf(
                passwordEntity(
                    id = 1L,
                    title = "Netflix",
                    login = "joao@email.com",
                    category = "Streaming",
                    categoryId = 3L,
                    encryptedPassword = "cipher-1",
                    passwordIv = "iv-1",
                    createdAt = 100L,
                    updatedAt = 200L
                ),
                passwordEntity(
                    id = 2L,
                    title = "GitHub",
                    login = "jsilva_dev",
                    category = "Work",
                    categoryId = null,
                    encryptedPassword = "cipher-2",
                    passwordIv = "iv-2",
                    createdAt = 300L,
                    updatedAt = 400L
                )
            )
        )
        val repository = buildRepository(localDataSource = localDataSource)

        val observed = repository.observePasswords().first()

        assertEquals(
            listOf(
                PasswordSummary(
                    id = 1L,
                    title = "Netflix",
                    login = "joao@email.com",
                    categoryId = 3L,
                    categoryName = "Streaming"
                ),
                PasswordSummary(
                    id = 2L,
                    title = "GitHub",
                    login = "jsilva_dev",
                    categoryId = null,
                    categoryName = "Work"
                )
            ),
            observed
        )
    }

    @Test
    fun givenLocalUpdates_whenObservingPasswords_thenEmitsMappedUpdatesReactively() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(initialPasswords = emptyList())
        val repository = buildRepository(localDataSource = localDataSource)

        localDataSource.emit(
            listOf(
                passwordEntity(
                    id = 8L,
                    title = "Spotify",
                    login = "premium_family_admin",
                    category = "Music",
                    categoryId = 12L,
                    encryptedPassword = "cipher",
                    passwordIv = "iv",
                    createdAt = 500L,
                    updatedAt = 600L
                )
            )
        )

        val observed = repository.observePasswords().first()

        assertEquals(
            listOf(
                PasswordSummary(
                    id = 8L,
                    title = "Spotify",
                    login = "premium_family_admin",
                    categoryId = 12L,
                    categoryName = "Music"
                )
            ),
            observed
        )
    }

    @Test
    fun givenEncryptedPasswords_whenObservingSecuritySnapshots_thenDecryptsAndResolvesFingerprints() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(
            initialPasswords = listOf(
                passwordEntity(
                    id = 1L,
                    title = "Netflix",
                    login = "joao@email.com",
                    category = "Streaming",
                    categoryId = 3L,
                    encryptedPassword = "cipher-1",
                    passwordIv = "iv-1",
                    createdAt = 100L,
                    updatedAt = 200L,
                    passwordFingerprint = "fp::stored-1"
                ),
                passwordEntity(
                    id = 2L,
                    title = "GitHub",
                    login = "jsilva_dev",
                    category = "Work",
                    categoryId = null,
                    encryptedPassword = "cipher-2",
                    passwordIv = "iv-2",
                    createdAt = 300L,
                    updatedAt = 400L,
                    passwordFingerprint = null
                )
            )
        )
        val fingerprintGenerator = FakePasswordFingerprintGenerator()
        val repository = buildRepository(
            localDataSource = localDataSource,
            passwordFingerprintGenerator = fingerprintGenerator
        )

        val observed = repository.observePasswordSecuritySnapshots().first()

        assertEquals(
            listOf(
                PasswordSecuritySnapshot(
                    password = "plain::cipher-1",
                    fingerprint = "fp::stored-1"
                ),
                PasswordSecuritySnapshot(
                    password = "plain::cipher-2",
                    fingerprint = "fp::plain::cipher-2"
                )
            ),
            observed
        )
        assertEquals("plain::cipher-2", fingerprintGenerator.lastFingerprintedPassword)
    }

    @Test
    fun givenNewPassword_whenCreating_thenEncryptsAndFingerprintsBeforePersisting() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(initialPasswords = emptyList())
        val passwordCipher = FakePasswordCipher()
        val fingerprintGenerator = FakePasswordFingerprintGenerator()
        val repository = buildRepository(
            localDataSource = localDataSource,
            passwordCipher = passwordCipher,
            passwordFingerprintGenerator = fingerprintGenerator
        )

        repository.createPassword(
            NewPassword(
                title = "GitHub",
                login = "dev@empresa.com",
                categoryId = 11L,
                categoryName = "Work",
                password = "plain-secret",
                note = "Conta usada pelo time de plataforma",
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_700_000_000_000L
            )
        )

        assertEquals("enc::plain-secret", localDataSource.insertedPassword?.encryptedPassword)
        assertEquals("iv::plain-secret", localDataSource.insertedPassword?.passwordIv)
        assertEquals("fp::plain-secret", localDataSource.insertedPassword?.passwordFingerprint)
        assertEquals(11L, localDataSource.insertedPassword?.categoryId)
        assertEquals("Work", localDataSource.insertedPassword?.category)
        assertEquals("Conta usada pelo time de plataforma", localDataSource.insertedPassword?.note)
        assertEquals(1_700_000_000_000L, localDataSource.insertedPassword?.createdAt)
        assertEquals(1_700_000_000_000L, localDataSource.insertedPassword?.updatedAt)
        assertEquals("plain-secret", passwordCipher.lastEncryptedPlainText)
        assertEquals("plain-secret", fingerprintGenerator.lastFingerprintedPassword)
    }

    @Test
    fun givenCipherFailure_whenCreating_thenPropagatesErrorWithoutPersistingPlainText() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(initialPasswords = emptyList())
        val repository = buildRepository(
            localDataSource = localDataSource,
            passwordCipher = object : PasswordCipher {
                override fun encrypt(plainText: String): EncryptedPasswordPayload {
                    error("cipher failure")
                }

                override fun decrypt(cipherText: String, iv: String, version: Int): String {
                    error("unused")
                }
            }
        )

        val thrown = try {
            repository.createPassword(
                NewPassword(
                    title = "GitHub",
                    login = "",
                    categoryId = null,
                    categoryName = null,
                    password = "plain-secret",
                    createdAt = 1_700_000_000_000L,
                    updatedAt = 1_700_000_000_000L,
                    note = null
                )
            )
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertTrue(thrown is IllegalStateException)
        assertEquals(null, localDataSource.insertedPassword)
    }

    @Test
    fun givenCountRequest_whenQueryingRepository_thenDelegatesToLocalDataSource() = runTest {
        val repository = buildRepository(
            localDataSource = FakePasswordsLocalDataSource(
                initialPasswords = emptyList(),
                passwordCount = 5
            )
        )

        assertEquals(5, repository.getPasswordCount())
    }

    @Test
    fun givenCategoryId_whenObservingPasswordsByCategory_thenDelegatesReactiveFilterToLocalDataSource() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(initialPasswords = emptyList())
        val repository = buildRepository(localDataSource = localDataSource)
        localDataSource.emitByCategory(
            listOf(
                passwordEntity(
                    id = 21L,
                    title = "Notion",
                    login = "time@team.com",
                    category = "Work",
                    categoryId = 7L,
                    encryptedPassword = "cipher",
                    passwordIv = "iv",
                    createdAt = 700L,
                    updatedAt = 800L
                )
            )
        )

        val observed = repository.observePasswordsByCategoryId(7L).first()

        assertEquals(7L, localDataSource.lastObservedCategoryId)
        assertEquals("Notion", observed.single().title)
        assertEquals(7L, observed.single().categoryId)
        assertEquals("Work", observed.single().categoryName)
    }

    @Test
    fun givenLocalDataSourceFailure_whenObservingPasswords_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("local source failure")
        val repository = buildRepository(
            localDataSource = object : PasswordsLocalDataSource {
                override fun observePasswords(): Flow<List<PasswordEntity>> = flow {
                    throw expected
                }

                override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>> =
                    flow { emit(emptyList()) }

                override suspend fun createPassword(password: PasswordEntity): Long = 0L

                override suspend fun getPasswordById(passwordId: Long): PasswordEntity? = null

                override suspend fun updatePassword(password: PasswordEntity) = Unit

                override suspend fun getPasswordCount(): Int = 0

                override suspend fun countPasswordsWithFingerprint(
                    passwordFingerprint: String,
                    excludePasswordId: Long?
                ): Int = 0

                override suspend fun getPasswordsMissingFingerprint(): List<PasswordEntity> = emptyList()

                override suspend fun updatePasswordFingerprint(
                    passwordId: Long,
                    passwordFingerprint: String
                ) = Unit
            }
        )

        val thrown = try {
            repository.observePasswords().first()
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertTrue(thrown is IllegalStateException)
        assertEquals(expected.message, thrown?.message)
    }

    @Test
    fun givenPersistedEncryptedPassword_whenRequestingDetails_thenDecryptsAndMapsFullModel() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(
            initialPasswords = emptyList(),
            passwordById = passwordEntity(
                id = 31L,
                title = "Spotify",
                login = "premium@vault.com",
                category = "Music",
                categoryId = 7L,
                encryptedPassword = "cipher",
                passwordIv = "iv",
                iconKey = "sp",
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_710_000_000_000L,
                note = "Renovar no fim do trimestre",
                passwordFingerprint = null
            )
        )
        val repository = buildRepository(localDataSource = localDataSource)

        val result = repository.getPasswordDetails(31L)

        assertEquals(
            PasswordDetails(
                id = 31L,
                title = "Spotify",
                login = "premium@vault.com",
                password = "plain::cipher",
                categoryId = 7L,
                categoryName = "Music",
                iconKey = "sp",
                note = "Renovar no fim do trimestre",
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_710_000_000_000L
            ),
            result
        )
        assertEquals(31L, localDataSource.lastFingerprintUpdatePasswordId)
        assertEquals("fp::plain::cipher", localDataSource.lastFingerprintUpdateValue)
    }

    @Test
    fun givenUpdatedPassword_whenPersisting_thenReEncryptsFingerprintsAndPreservesCreatedAt() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(initialPasswords = emptyList())
        val repository = buildRepository(localDataSource = localDataSource)

        repository.updatePassword(
            PasswordDetails(
                id = 31L,
                title = "Spotify",
                login = "updated@vault.com",
                password = "new-secret",
                note = "Conta rotacionada em janeiro",
                categoryId = 7L,
                categoryName = "Music",
                iconKey = "sp",
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_760_000_000_000L
            )
        )

        assertEquals("enc::new-secret", localDataSource.updatedPassword?.encryptedPassword)
        assertEquals("iv::new-secret", localDataSource.updatedPassword?.passwordIv)
        assertEquals("fp::new-secret", localDataSource.updatedPassword?.passwordFingerprint)
        assertEquals("Conta rotacionada em janeiro", localDataSource.updatedPassword?.note)
        assertEquals(1_700_000_000_000L, localDataSource.updatedPassword?.createdAt)
        assertEquals(1_760_000_000_000L, localDataSource.updatedPassword?.updatedAt)
    }

    @Test
    fun givenDuplicateLookup_whenMissingFingerprintsExist_thenBackfillsBeforeCounting() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(
            initialPasswords = emptyList(),
            duplicateCount = 1,
            passwordsMissingFingerprint = listOf(
                passwordEntity(
                    id = 5L,
                    title = "Legacy",
                    login = "legacy@vault.com",
                    category = "Legacy",
                    categoryId = null,
                    encryptedPassword = "enc::legacy-secret",
                    passwordIv = "iv::legacy-secret",
                    createdAt = 100L,
                    updatedAt = 200L,
                    passwordFingerprint = null
                )
            )
        )
        val repository = buildRepository(localDataSource = localDataSource)

        val duplicate = repository.hasPasswordDuplicate(
            password = "legacy-secret",
            excludePasswordId = 8L
        )

        assertTrue(duplicate)
        assertEquals(5L, localDataSource.lastFingerprintUpdatePasswordId)
        assertEquals("fp::plain::enc::legacy-secret", localDataSource.lastFingerprintUpdateValue)
        assertEquals("fp::legacy-secret", localDataSource.lastDuplicateFingerprint)
        assertEquals(8L, localDataSource.lastDuplicateExcludedId)
    }

    private fun buildRepository(
        localDataSource: PasswordsLocalDataSource,
        passwordCipher: PasswordCipher = FakePasswordCipher(),
        passwordFingerprintGenerator: PasswordFingerprintGenerator = FakePasswordFingerprintGenerator(),
        timeProvider: TimeProvider = FixedTimeProvider(1_700_000_000_000L)
    ) = PasswordRepositoryImpl(
        localDataSource = localDataSource,
        passwordCipher = passwordCipher,
        passwordFingerprintGenerator = passwordFingerprintGenerator,
        timeProvider = timeProvider
    )

    private fun passwordEntity(
        id: Long,
        title: String,
        login: String,
        category: String,
        categoryId: Long?,
        encryptedPassword: String,
        passwordIv: String,
        iconKey: String = "",
        createdAt: Long,
        updatedAt: Long,
        note: String? = null,
        passwordFingerprint: String? = "fp::stored"
    ) = PasswordEntity(
        id = id,
        title = title,
        login = login,
        category = category,
        categoryId = categoryId,
        encryptedPassword = encryptedPassword,
        passwordIv = passwordIv,
        passwordCipherVersion = 1,
        iconKey = iconKey,
        createdAt = createdAt,
        updatedAt = updatedAt,
        note = note,
        passwordFingerprint = passwordFingerprint
    )

    private class FakePasswordsLocalDataSource(
        initialPasswords: List<PasswordEntity>,
        private val passwordCount: Int = 0,
        private val passwordById: PasswordEntity? = null,
        private val duplicateCount: Int = 0,
        private val passwordsMissingFingerprint: List<PasswordEntity> = emptyList()
    ) : PasswordsLocalDataSource {

        private val passwordsFlow = MutableStateFlow(initialPasswords)
        private val passwordsByCategoryFlow = MutableStateFlow<List<PasswordEntity>>(emptyList())
        var insertedPassword: PasswordEntity? = null
        var updatedPassword: PasswordEntity? = null
        var lastObservedCategoryId: Long? = null
        var lastDuplicateFingerprint: String? = null
        var lastDuplicateExcludedId: Long? = null
        var lastFingerprintUpdatePasswordId: Long? = null
        var lastFingerprintUpdateValue: String? = null

        override fun observePasswords(): Flow<List<PasswordEntity>> = passwordsFlow

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>> {
            lastObservedCategoryId = categoryId
            return passwordsByCategoryFlow
        }

        override suspend fun createPassword(password: PasswordEntity): Long {
            insertedPassword = password
            return 1L
        }

        override suspend fun getPasswordById(passwordId: Long): PasswordEntity? = passwordById

        override suspend fun updatePassword(password: PasswordEntity) {
            updatedPassword = password
        }

        override suspend fun getPasswordCount(): Int = passwordCount

        override suspend fun countPasswordsWithFingerprint(
            passwordFingerprint: String,
            excludePasswordId: Long?
        ): Int {
            lastDuplicateFingerprint = passwordFingerprint
            lastDuplicateExcludedId = excludePasswordId
            return duplicateCount
        }

        override suspend fun getPasswordsMissingFingerprint(): List<PasswordEntity> =
            passwordsMissingFingerprint

        override suspend fun updatePasswordFingerprint(passwordId: Long, passwordFingerprint: String) {
            lastFingerprintUpdatePasswordId = passwordId
            lastFingerprintUpdateValue = passwordFingerprint
        }

        fun emit(passwords: List<PasswordEntity>) {
            passwordsFlow.value = passwords
        }

        fun emitByCategory(passwords: List<PasswordEntity>) {
            passwordsByCategoryFlow.value = passwords
        }
    }

    private class FakePasswordCipher : PasswordCipher {
        var lastEncryptedPlainText: String? = null

        override fun encrypt(plainText: String): EncryptedPasswordPayload = EncryptedPasswordPayload(
            cipherText = "enc::$plainText",
            iv = "iv::$plainText",
            version = 1
        ).also {
            lastEncryptedPlainText = plainText
        }

        override fun decrypt(cipherText: String, iv: String, version: Int): String {
            return "plain::$cipherText"
        }
    }

    private class FakePasswordFingerprintGenerator : PasswordFingerprintGenerator {
        var lastFingerprintedPassword: String? = null

        override fun generate(password: String): String {
            lastFingerprintedPassword = password
            return "fp::$password"
        }
    }

    private class FixedTimeProvider(
        private val currentTimeMillis: Long
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = currentTimeMillis
    }
}
