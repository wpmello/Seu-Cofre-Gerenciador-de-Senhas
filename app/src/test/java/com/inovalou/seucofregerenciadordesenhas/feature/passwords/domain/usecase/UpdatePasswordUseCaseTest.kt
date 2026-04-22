package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePasswordUseCaseTest {

    @Test
    fun givenBlankPassword_whenInvoked_thenReturnsValidationError() = runTest {
        val repository = FakePasswordRepository(passwordDetails = persistedPassword())
        val useCase = UpdatePasswordUseCase(
            passwordRepository = repository,
            timeProvider = FixedTimeProvider(1_750_000_000_000L)
        )

        val result = useCase(
            passwordId = 8L,
            login = "editado@vault.com",
            password = "   "
        )

        assertTrue(result is UpdatePasswordResult.ValidationError)
        assertEquals(
            UpdatePasswordPasswordError.Blank,
            (result as UpdatePasswordResult.ValidationError).validation.passwordError
        )
        assertNull(repository.updatedPassword)
    }

    @Test
    fun givenMissingPassword_whenInvoked_thenReturnsNotFound() = runTest {
        val useCase = UpdatePasswordUseCase(
            passwordRepository = FakePasswordRepository(passwordDetails = null),
            timeProvider = FixedTimeProvider(1_750_000_000_000L)
        )

        assertEquals(
            UpdatePasswordResult.NotFound,
            useCase(passwordId = 44L, login = "", password = "new-secret")
        )
    }

    @Test
    fun givenPersistedPassword_whenInvoked_thenKeepsCreatedAtAndRefreshesUpdatedAt() = runTest {
        val repository = FakePasswordRepository(passwordDetails = persistedPassword())
        val useCase = UpdatePasswordUseCase(
            passwordRepository = repository,
            timeProvider = FixedTimeProvider(1_750_000_000_000L)
        )

        val result = useCase(
            passwordId = 8L,
            login = "  novo@email.com  ",
            password = "new-secret"
        )

        assertEquals(UpdatePasswordResult.Success, result)
        assertEquals(
            persistedPassword().copy(
                login = "novo@email.com",
                password = "new-secret",
                updatedAt = 1_750_000_000_000L
            ),
            repository.updatedPassword
        )
    }

    @Test
    fun givenRepositoryFailure_whenInvoked_thenReturnsFailure() = runTest {
        val useCase = UpdatePasswordUseCase(
            passwordRepository = FakePasswordRepository(
                passwordDetails = persistedPassword(),
                shouldFailOnUpdate = true
            ),
            timeProvider = FixedTimeProvider(1_750_000_000_000L)
        )

        assertEquals(
            UpdatePasswordResult.Failure,
            useCase(passwordId = 8L, login = "mail", password = "new-secret")
        )
    }

    private fun persistedPassword() = PasswordDetails(
        id = 8L,
        title = "Spotify",
        login = "premium@vault.com",
        password = "old-secret",
        categoryId = 2L,
        categoryName = "Music",
        iconKey = "sp",
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_710_000_000_000L
    )

    private class FakePasswordRepository(
        private val passwordDetails: PasswordDetails?,
        private val shouldFailOnUpdate: Boolean = false
    ) : PasswordRepository {

        var updatedPassword: PasswordDetails? = null

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = passwordDetails

        override suspend fun updatePassword(password: PasswordDetails) {
            if (shouldFailOnUpdate) {
                error("update failure")
            }
            updatedPassword = password
        }
    }

    private class FixedTimeProvider(
        private val currentTimeMillis: Long
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = currentTimeMillis
    }
}
