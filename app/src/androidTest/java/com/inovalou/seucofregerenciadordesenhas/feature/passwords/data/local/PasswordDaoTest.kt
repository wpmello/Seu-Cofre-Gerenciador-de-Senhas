package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordDaoTest {

    private lateinit var database: SeuCofreDatabase
    private lateinit var passwordDao: PasswordDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            SeuCofreDatabase::class.java
        ).build()
        passwordDao = database.passwordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenNoPersistedPasswords_whenObserving_thenReturnsEmptyList() = runTest {
        val passwords = passwordDao.observePasswords().first()

        assertTrue(passwords.isEmpty())
    }

    @Test
    fun givenPersistedPasswords_whenObserving_thenReturnsTitleOrderedListIgnoringCase() = runTest {
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, icon_key, item_count) VALUES (9, 'Work', 'ic_directory', 0)"
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (1, 'zeta', 'z@email.com', 'Misc', NULL, 'cipher-z', 'iv-z', 1, '', 'Primeira conta', 100, 200)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (2, 'Alpha', 'a@email.com', '', 9, 'cipher-a', 'iv-a', 1, '', NULL, 300, 400)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (3, 'bravo', 'b@email.com', 'Private', NULL, 'cipher-b', 'iv-b', 1, '', 'Backup', 500, 600)
            """.trimIndent()
        )

        val passwords = passwordDao.observePasswords().first()

        assertEquals(listOf("Alpha", "bravo", "zeta"), passwords.map { it.title })
        assertEquals(listOf("a@email.com", "b@email.com", "z@email.com"), passwords.map { it.login })
        assertEquals(listOf("Work", "Private", "Misc"), passwords.map { it.category })
        assertEquals(listOf(9L, null, null), passwords.map { it.categoryId })
        assertEquals(listOf(null, "Backup", "Primeira conta"), passwords.map { it.note })
        assertEquals(listOf("cipher-a", "cipher-b", "cipher-z"), passwords.map { it.encryptedPassword })
        assertEquals(listOf(300L, 500L, 100L), passwords.map { it.createdAt })
        assertEquals(listOf(400L, 600L, 200L), passwords.map { it.updatedAt })
    }

    @Test
    fun givenPasswordsAssociatedToCategory_whenObservingByCategoryId_thenReturnsOnlyMatchingPasswords() = runTest {
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, icon_key, item_count) VALUES (3, 'Streaming', 'ic_directory', 0)"
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (1, 'Netflix', 'a@email.com', '', 3, 'cipher-a', 'iv-a', 1, '', 'Perfil da TV', 100, 200)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (2, 'GitHub', 'dev@email.com', 'Legacy', NULL, 'cipher-b', 'iv-b', 1, '', NULL, 300, 400)
            """.trimIndent()
        )

        val passwords = passwordDao.observePasswordsByCategoryId(3L).first()

        assertEquals(1, passwords.size)
        assertEquals("Netflix", passwords.single().title)
        assertEquals("Streaming", passwords.single().category)
        assertEquals(3L, passwords.single().categoryId)
        assertEquals("Perfil da TV", passwords.single().note)
    }

    @Test
    fun givenPersistedPasswordId_whenQueryingById_thenReturnsFullRecordWithDates() = runTest {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (9, 'Spotify', 'premium@vault.com', 'Music', NULL, 'cipher', 'iv', 1, 'sp', 'Renovar em julho', 111, 222)
            """.trimIndent()
        )

        val password = passwordDao.getPasswordById(9L)

        assertEquals(9L, password?.id)
        assertEquals("Renovar em julho", password?.note)
        assertEquals(111L, password?.createdAt)
        assertEquals(222L, password?.updatedAt)
    }

    @Test
    fun givenPersistedPasswords_whenObservingRecent_thenOrdersByLatestCreatedOrUpdatedDateAndLimits() = runTest {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (1, 'Old', 'old@vault.com', 'Misc', NULL, 'cipher-old', 'iv-old', 1, '', NULL, 10, 11)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (2, 'Updated', 'updated@vault.com', 'Misc', NULL, 'cipher-updated', 'iv-updated', 1, '', NULL, 20, 90)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (3, 'Created', 'created@vault.com', 'Misc', NULL, 'cipher-created', 'iv-created', 1, '', NULL, 80, 30)
            """.trimIndent()
        )

        val recent = passwordDao.observeRecentPasswords(limit = 2).first()

        assertEquals(listOf("Updated", "Created"), recent.map { it.title })
    }

    @Test
    fun givenSearchPattern_whenObservingPasswordSearchResults_thenReturnsTitleMatchesOnly() = runTest {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (1, 'Banco Digital', 'user@email.com', 'Financeiro', NULL, 'cipher-a', 'iv-a', 1, 'ic_bank', NULL, 100, 200)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (2, 'GitHub', 'banco@empresa.com', 'Work', NULL, 'cipher-b', 'iv-b', 1, 'ic_code', NULL, 300, 400)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (3, 'banco reserva', 'admin@email.com', 'Financeiro', NULL, 'cipher-c', 'iv-c', 1, '', NULL, 500, 600)
            """.trimIndent()
        )

        val passwords = passwordDao.observePasswordSearchResults("%banco%").first()

        assertEquals(listOf("Banco Digital", "banco reserva"), passwords.map { it.title })
        assertEquals(listOf("ic_bank", ""), passwords.map { it.iconKey })
    }

    @Test
    fun givenMatchingFingerprints_whenCountingDuplicates_thenIgnoresExcludedPassword() = runTest {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at, password_fingerprint
            ) VALUES (1, 'Spotify', 'a@vault.com', 'Music', NULL, 'cipher-a', 'iv-a', 1, '', NULL, 100, 200, 'fp-1')
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at, password_fingerprint
            ) VALUES (2, 'Netflix', 'b@vault.com', 'Streaming', NULL, 'cipher-b', 'iv-b', 1, '', NULL, 300, 400, 'fp-1')
            """.trimIndent()
        )

        val duplicateCount = passwordDao.countPasswordsWithFingerprint(
            passwordFingerprint = "fp-1",
            excludePasswordId = 1L
        )

        assertEquals(1, duplicateCount)
    }

    @Test
    fun givenLegacyPasswordsWithoutFingerprint_whenQueryingMissingFingerprints_thenReturnsOnlyLegacyRows() = runTest {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at, password_fingerprint
            ) VALUES (1, 'Legacy One', 'a@vault.com', 'Legacy', NULL, 'cipher-a', 'iv-a', 1, '', NULL, 100, 200, NULL)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at, password_fingerprint
            ) VALUES (2, 'Fresh One', 'b@vault.com', 'Fresh', NULL, 'cipher-b', 'iv-b', 1, '', NULL, 300, 400, 'fp-2')
            """.trimIndent()
        )

        val missing = passwordDao.getPasswordsMissingFingerprint()

        assertEquals(listOf(1L), missing.map { it.id })
    }

    @Test
    fun givenPersistedPassword_whenDeletingById_thenRemovesOnlyMatchingPasswordAndUpdatesObservers() = runTest {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (1, 'Spotify', 'premium@vault.com', 'Music', NULL, 'cipher-a', 'iv-a', 1, '', NULL, 100, 200)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, note, created_at, updated_at
            ) VALUES (2, 'GitHub', 'dev@vault.com', 'Work', NULL, 'cipher-b', 'iv-b', 1, '', NULL, 300, 400)
            """.trimIndent()
        )

        val affectedRows = passwordDao.deletePasswordById(1L)
        val remainingPasswords = passwordDao.observePasswords().first()
        val missingAffectedRows = passwordDao.deletePasswordById(404L)

        assertEquals(1, affectedRows)
        assertEquals(listOf("GitHub"), remainingPasswords.map { it.title })
        assertEquals(0, missingAffectedRows)
    }
}
