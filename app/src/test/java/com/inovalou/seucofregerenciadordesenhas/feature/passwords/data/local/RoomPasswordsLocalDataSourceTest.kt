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
            PasswordEntity(id = 1L, title = "Amazon Prime", login = "compras@email.com", iconKey = "ic_home"),
            PasswordEntity(id = 2L, title = "GitHub", login = "dev@empresa.com", iconKey = "ic_cloud")
        )
        val dataSource = RoomPasswordsLocalDataSource(passwordDao = FakePasswordDao(flowOf(expected)))

        val observed = dataSource.observePasswords().first()

        assertEquals(expected, observed)
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
        private val passwordsFlow: Flow<List<PasswordEntity>>
    ) : PasswordDao {

        override fun observePasswords(): Flow<List<PasswordEntity>> = passwordsFlow
    }
}
