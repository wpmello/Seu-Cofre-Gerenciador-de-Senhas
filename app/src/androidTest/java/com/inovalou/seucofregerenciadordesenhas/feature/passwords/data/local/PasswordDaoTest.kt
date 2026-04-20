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
            """
            INSERT INTO passwords(
                id, title, login, category, encrypted_password, password_iv, password_cipher_version, icon_key
            ) VALUES (1, 'zeta', 'z@email.com', 'Misc', 'cipher-z', 'iv-z', 1, '')
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, encrypted_password, password_iv, password_cipher_version, icon_key
            ) VALUES (2, 'Alpha', 'a@email.com', 'Work', 'cipher-a', 'iv-a', 1, '')
            """.trimIndent()
        )
        database.openHelper.writableDatabase.execSQL(
            """
            INSERT INTO passwords(
                id, title, login, category, encrypted_password, password_iv, password_cipher_version, icon_key
            ) VALUES (3, 'bravo', 'b@email.com', 'Private', 'cipher-b', 'iv-b', 1, '')
            """.trimIndent()
        )

        val passwords = passwordDao.observePasswords().first()

        assertEquals(listOf("Alpha", "bravo", "zeta"), passwords.map { it.title })
        assertEquals(listOf("a@email.com", "b@email.com", "z@email.com"), passwords.map { it.login })
        assertEquals(listOf("Work", "Private", "Misc"), passwords.map { it.category })
        assertEquals(listOf("cipher-a", "cipher-b", "cipher-z"), passwords.map { it.encryptedPassword })
    }
}
