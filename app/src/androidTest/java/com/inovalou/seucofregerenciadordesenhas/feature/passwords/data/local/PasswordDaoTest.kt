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
            "INSERT INTO passwords(id, title, login, icon_key) VALUES (1, 'zeta', 'z@email.com', 'ic_star')"
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO passwords(id, title, login, icon_key) VALUES (2, 'Alpha', 'a@email.com', 'ic_directory')"
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO passwords(id, title, login, icon_key) VALUES (3, 'bravo', 'b@email.com', 'ic_padlock')"
        )

        val passwords = passwordDao.observePasswords().first()

        assertEquals(listOf("Alpha", "bravo", "zeta"), passwords.map { it.title })
        assertEquals(listOf("a@email.com", "b@email.com", "z@email.com"), passwords.map { it.login })
        assertEquals(listOf("ic_directory", "ic_padlock", "ic_star"), passwords.map { it.iconKey })
    }
}
