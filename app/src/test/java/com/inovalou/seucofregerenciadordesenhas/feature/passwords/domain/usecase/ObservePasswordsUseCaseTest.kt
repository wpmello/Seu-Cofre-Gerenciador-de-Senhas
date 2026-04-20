package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservePasswordsUseCaseTest {

    @Test
    fun givenRepositoryStream_whenInvoked_thenReturnsObservedPasswords() = runTest {
        val expected = listOf(
            PasswordSummary(id = 3L, title = "Instagram", login = "@joao_viajante", iconKey = "ic_user_profile")
        )
        val useCase = ObservePasswordsUseCase(
            repository = object : PasswordRepository {
                override fun observePasswords(): Flow<List<PasswordSummary>> = flowOf(expected)
            }
        )

        assertEquals(expected, useCase().first())
    }
}
