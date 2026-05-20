package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetPasswordDetailsUseCaseTest {

    @Test
    fun givenPersistedPasswordId_whenInvoked_thenReturnsItsDetails() = runTest {
        val expected = PasswordDetails(
            id = 8L,
            title = "Spotify",
            login = "premium@vault.com",
            password = "plain-secret",
            categoryId = 2L,
            categoryName = "Music",
            iconKey = "sp",
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_710_000_000_000L,
            note = "Conta premium"
        )
        val useCase = GetPasswordDetailsUseCase(
            passwordRepository = FakePasswordRepository(passwordDetails = expected)
        )

        val result = useCase(passwordId = 8L)

        assertEquals(expected, result)
    }

    @Test
    fun givenMissingPasswordId_whenInvoked_thenReturnsNull() = runTest {
        val useCase = GetPasswordDetailsUseCase(
            passwordRepository = FakePasswordRepository(passwordDetails = null)
        )

        assertNull(useCase(passwordId = 99L))
    }

    private class FakePasswordRepository(
        private val passwordDetails: PasswordDetails?
    ) : PasswordRepository {


        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = passwordDetails

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false
    }
}
