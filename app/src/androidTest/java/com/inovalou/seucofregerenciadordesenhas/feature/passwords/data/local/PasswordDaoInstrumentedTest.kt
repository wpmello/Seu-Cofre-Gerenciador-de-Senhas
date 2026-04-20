package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordDaoInstrumentedTest {

    private lateinit var database: SeuCofreDatabase
    private lateinit var passwordDao: PasswordDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, SeuCofreDatabase::class.java).build()
        passwordDao = database.passwordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenInsertedPassword_whenObservingAndCounting_thenPersistsEncryptedFields() = runTest {
        passwordDao.insert(
            PasswordEntity(
                title = "GitHub",
                login = "dev@empresa.com",
                category = "Work",
                encryptedPassword = "cipher-text",
                passwordIv = "iv-text",
                passwordCipherVersion = 1,
                iconKey = ""
            )
        )

        val stored = passwordDao.observePasswords().first().single()

        assertEquals(1, passwordDao.countPasswords())
        assertEquals("GitHub", stored.title)
        assertEquals("cipher-text", stored.encryptedPassword)
        assertEquals("iv-text", stored.passwordIv)
    }
}
