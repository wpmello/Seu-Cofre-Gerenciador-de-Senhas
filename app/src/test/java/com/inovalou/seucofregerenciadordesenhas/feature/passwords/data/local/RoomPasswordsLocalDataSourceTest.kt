package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomPasswordsLocalDataSourceTest {

    @Test
    fun givenDaoFlow_whenObservingPasswords_thenDelegatesToDao() = runTest {
        val expected = listOf(
            PasswordEntity(
                id = 1L,
                title = "Amazon Prime",
                login = "compras@email.com",
                category = "Shopping",
                categoryId = 2L,
                encryptedPassword = "cipher-1",
                passwordIv = "iv-1",
                passwordCipherVersion = 1,
                iconKey = "",
                createdAt = 1L,
                updatedAt = 2L
            ),
            PasswordEntity(
                id = 2L,
                title = "GitHub",
                login = "dev@empresa.com",
                category = "Work",
                categoryId = 4L,
                encryptedPassword = "cipher-2",
                passwordIv = "iv-2",
                passwordCipherVersion = 1,
                iconKey = "",
                createdAt = 3L,
                updatedAt = 4L
            )
        )
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = FakePasswordDao(flowOf(expected)))

        val observed = dataSource.observePasswords().first()

        assertEquals(expected, observed)
    }

    @Test
    fun givenEntity_whenCreatingPassword_thenDelegatesInsertToDao() = runTest {
        val dao = FakePasswordDao(flowOf(emptyList()))
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = dao)
        val entity = PasswordEntity(
            title = "GitHub",
            login = "dev@empresa.com",
            category = "Work",
            categoryId = 3L,
            encryptedPassword = "cipher",
            passwordIv = "iv",
            passwordCipherVersion = 1,
            iconKey = "",
            createdAt = 10L,
            updatedAt = 20L
        )

        val insertedId = dataSource.createPassword(entity)

        assertEquals(7L, insertedId)
        assertEquals(entity, dao.insertedEntity)
    }

    @Test
    fun givenCategoryId_whenObservingPasswordsByCategory_thenDelegatesCategoryQueryToDao() = runTest {
        val expected = listOf(
            PasswordEntity(
                id = 3L,
                title = "GitHub",
                login = "dev@empresa.com",
                category = "Work",
                categoryId = 9L,
                encryptedPassword = "cipher",
                passwordIv = "iv",
                passwordCipherVersion = 1,
                iconKey = "",
                createdAt = 30L,
                updatedAt = 40L
            )
        )
        val dao = FakePasswordDao(
            passwordsFlow = flowOf(emptyList()),
            passwordsByCategoryFlow = flowOf(expected)
        )
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = dao)

        val observed = dataSource.observePasswordsByCategoryId(9L).first()

        assertEquals(9L, dao.lastObservedCategoryId)
        assertEquals(expected, observed)
    }

    @Test
    fun givenDaoCount_whenRequestingPasswordCount_thenDelegatesToDao() = runTest {
        val dataSource = RoomPasswordsLocalDataSource(
            passwordDao = FakePasswordDao(flowOf(emptyList()), passwordCount = 4)
        )

        assertEquals(4, dataSource.getPasswordCount())
    }

    @Test
    fun givenPasswordId_whenQueryingDetails_thenDelegatesLookupToDao() = runTest {
        val expected = PasswordEntity(
            id = 11L,
            title = "Spotify",
            login = "premium@vault.com",
            category = "Music",
            categoryId = 5L,
            encryptedPassword = "cipher",
            passwordIv = "iv",
            passwordCipherVersion = 1,
            iconKey = "sp",
            createdAt = 100L,
            updatedAt = 200L
        )
        val dao = FakePasswordDao(flowOf(emptyList()), passwordById = expected)
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = dao)

        val observed = dataSource.getPasswordById(11L)

        assertEquals(11L, dao.lastRequestedPasswordId)
        assertEquals(expected, observed)
    }

    @Test
    fun givenUpdatedPassword_whenPersisting_thenDelegatesUpdateToDao() = runTest {
        val dao = FakePasswordDao(flowOf(emptyList()))
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = dao)
        val entity = PasswordEntity(
            id = 12L,
            title = "Spotify",
            login = "updated@vault.com",
            category = "Music",
            categoryId = 5L,
            encryptedPassword = "cipher",
            passwordIv = "iv",
            passwordCipherVersion = 2,
            iconKey = "sp",
            createdAt = 100L,
            updatedAt = 400L
        )

        dataSource.updatePassword(entity)

        assertEquals(entity, dao.updatedEntity)
    }

    @Test
    fun givenFingerprintLookup_whenCountingDuplicates_thenDelegatesQueryToDao() = runTest {
        val dao = FakePasswordDao(flowOf(emptyList()), duplicateCount = 2)
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = dao)

        val count = dataSource.countPasswordsWithFingerprint(
            passwordFingerprint = "fp-123",
            excludePasswordId = 9L
        )

        assertEquals(2, count)
        assertEquals("fp-123", dao.lastDuplicateFingerprint)
        assertEquals(9L, dao.lastDuplicateExcludedId)
    }

    @Test
    fun givenMissingFingerprints_whenRequested_thenDelegatesLookupToDao() = runTest {
        val expected = listOf(
            PasswordEntity(
                id = 21L,
                title = "Legacy",
                login = "legacy@vault.com",
                category = "Work",
                categoryId = 3L,
                encryptedPassword = "cipher",
                passwordIv = "iv",
                passwordCipherVersion = 1,
                iconKey = "",
                createdAt = 10L,
                updatedAt = 20L,
                note = null,
                passwordFingerprint = null
            )
        )
        val dao = FakePasswordDao(flowOf(emptyList()), passwordsMissingFingerprint = expected)
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = dao)

        val missing = dataSource.getPasswordsMissingFingerprint()

        assertEquals(expected, missing)
    }

    @Test
    fun givenFingerprintUpdate_whenPersistingFingerprint_thenDelegatesUpdateToDao() = runTest {
        val dao = FakePasswordDao(flowOf(emptyList()))
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = dao)

        dataSource.updatePasswordFingerprint(passwordId = 11L, passwordFingerprint = "fp-11")

        assertEquals(11L, dao.lastUpdatedFingerprintPasswordId)
        assertEquals("fp-11", dao.lastUpdatedFingerprint)
    }

    @Test
    fun givenDaoFailure_whenObservingPasswords_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("database unavailable")
        val dataSource = RoomPasswordsLocalDataSource(
            passwordDao = FakePasswordDao(
                flow {
                    throw expected
                }
            )
        )

        val thrown = try {
            dataSource.observePasswords().first()
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertTrue(thrown is IllegalStateException)
        assertEquals(expected.message, thrown?.message)
    }

    private class FakePasswordDao(
        private val passwordsFlow: Flow<List<PasswordEntity>>,
        private val passwordsByCategoryFlow: Flow<List<PasswordEntity>> = flowOf(emptyList()),
        private val passwordCount: Int = 0,
        private val passwordById: PasswordEntity? = null,
        private val duplicateCount: Int = 0,
        private val passwordsMissingFingerprint: List<PasswordEntity> = emptyList()
    ) : PasswordDao {

        var insertedEntity: PasswordEntity? = null
        var updatedEntity: PasswordEntity? = null
        var lastObservedCategoryId: Long? = null
        var lastRequestedPasswordId: Long? = null
        var lastDuplicateFingerprint: String? = null
        var lastDuplicateExcludedId: Long? = null
        var lastUpdatedFingerprintPasswordId: Long? = null
        var lastUpdatedFingerprint: String? = null

        override fun observePasswords(): Flow<List<PasswordEntity>> = passwordsFlow

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>> {
            lastObservedCategoryId = categoryId
            return passwordsByCategoryFlow
        }

        override suspend fun insert(password: PasswordEntity): Long {
            insertedEntity = password
            return 7L
        }

        override suspend fun getPasswordById(passwordId: Long): PasswordEntity? {
            lastRequestedPasswordId = passwordId
            return passwordById
        }

        override suspend fun update(password: PasswordEntity) {
            updatedEntity = password
        }

        override suspend fun countPasswords(): Int = passwordCount

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
            lastUpdatedFingerprintPasswordId = passwordId
            lastUpdatedFingerprint = passwordFingerprint
        }
    }
}
