package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.repository

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordsLocalDataSource
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
            listOf(
                PasswordEntity(id = 1L, title = "Netflix", login = "joao@email.com", iconKey = "ic_home"),
                PasswordEntity(id = 2L, title = "GitHub", login = "jsilva_dev", iconKey = "ic_cloud")
            )
        )
        val repository = PasswordRepositoryImpl(localDataSource)

        val observed = repository.observePasswords().first()

        assertEquals(
            listOf(
                PasswordSummary(id = 1L, title = "Netflix", login = "joao@email.com", iconKey = "ic_home"),
                PasswordSummary(id = 2L, title = "GitHub", login = "jsilva_dev", iconKey = "ic_cloud")
            ),
            observed
        )
    }

    @Test
    fun givenLocalUpdates_whenObservingPasswords_thenEmitsMappedUpdatesReactively() = runTest {
        val localDataSource = FakePasswordsLocalDataSource(emptyList())
        val repository = PasswordRepositoryImpl(localDataSource)

        localDataSource.emit(
            listOf(
                PasswordEntity(id = 8L, title = "Spotify", login = "premium_family_admin", iconKey = "ic_favorite")
            )
        )

        val observed = repository.observePasswords().first()

        assertEquals(
            listOf(
                PasswordSummary(
                    id = 8L,
                    title = "Spotify",
                    login = "premium_family_admin",
                    iconKey = "ic_favorite"
                )
            ),
            observed
        )
    }

    @Test
    fun givenLocalDataSourceFailure_whenObservingPasswords_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("local source failure")
        val repository = PasswordRepositoryImpl(
            object : PasswordsLocalDataSource {
                override fun observePasswords(): Flow<List<PasswordEntity>> = flow {
                    throw expected
                }
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

    private class FakePasswordsLocalDataSource(
        initialPasswords: List<PasswordEntity>
    ) : PasswordsLocalDataSource {

        private val passwordsFlow = MutableStateFlow(initialPasswords)

        override fun observePasswords(): Flow<List<PasswordEntity>> = passwordsFlow

        fun emit(passwords: List<PasswordEntity>) {
            passwordsFlow.value = passwords
        }
    }
}
