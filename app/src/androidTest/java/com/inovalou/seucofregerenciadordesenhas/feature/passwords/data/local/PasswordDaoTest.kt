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
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, created_at, updated_at
            ) VALUES (1, 'zeta', 'z@email.com', 'Misc', NULL, 'cipher-z', 'iv-z', 1, '', 100, 200)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, created_at, updated_at
            ) VALUES (2, 'Alpha', 'a@email.com', '', 9, 'cipher-a', 'iv-a', 1, '', 300, 400)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, created_at, updated_at
            ) VALUES (3, 'bravo', 'b@email.com', 'Private', NULL, 'cipher-b', 'iv-b', 1, '', 500, 600)
            """.trimIndent()
        )

        val passwords = passwordDao.observePasswords().first()

        assertEquals(listOf("Alpha", "bravo", "zeta"), passwords.map { it.title })
        assertEquals(listOf("a@email.com", "b@email.com", "z@email.com"), passwords.map { it.login })
        assertEquals(listOf("Work", "Private", "Misc"), passwords.map { it.category })
        assertEquals(listOf(9L, null, null), passwords.map { it.categoryId })
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
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, created_at, updated_at
            ) VALUES (1, 'Netflix', 'a@email.com', '', 3, 'cipher-a', 'iv-a', 1, '', 100, 200)
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, created_at, updated_at
            ) VALUES (2, 'GitHub', 'dev@email.com', 'Legacy', NULL, 'cipher-b', 'iv-b', 1, '', 300, 400)
            """.trimIndent()
        )

        val passwords = passwordDao.observePasswordsByCategoryId(3L).first()

        assertEquals(1, passwords.size)
        assertEquals("Netflix", passwords.single().title)
        assertEquals("Streaming", passwords.single().category)
        assertEquals(3L, passwords.single().categoryId)
    }

    @Test
    fun givenPersistedPasswordId_whenQueryingById_thenReturnsFullRecordWithDates() = runTest {
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, created_at, updated_at
            ) VALUES (9, 'Spotify', 'premium@vault.com', 'Music', NULL, 'cipher', 'iv', 1, 'sp', 111, 222)
            """.trimIndent()
        )

        val password = passwordDao.getPasswordById(9L)

        assertEquals(9L, password?.id)
        assertEquals(111L, password?.createdAt)
        assertEquals(222L, password?.updatedAt)
    }
}
