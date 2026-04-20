package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePasswordUseCaseTest {

    @Test
    fun givenBlankPassword_whenInvoked_thenReturnsValidationError() = runTest {
        val repository = FakePasswordRepository()
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository)
        )

        val result = useCase(
            title = "Netflix",
            login = "joao@email.com",
            category = "Streaming",
            password = "   "
        )

        assertTrue(result is CreatePasswordResult.ValidationError)
        assertEquals(
            CreatePasswordPasswordError.Blank,
            (result as CreatePasswordResult.ValidationError).validation.passwordError
        )
        assertNull(repository.createdPassword)
    }

    @Test
    fun givenBlankTitle_whenInvoked_thenCreatesPasswordWithAutomaticAppName() = runTest {
        val repository = FakePasswordRepository(passwordCount = 1)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository)
        )

        val result = useCase(
            title = "   ",
            login = "  joao@email.com  ",
            category = "  Trabalho  ",
            password = " senha super secreta "
        )

        assertEquals(CreatePasswordResult.Success, result)
        assertEquals(
            NewPassword(
                title = "App 2",
                login = "joao@email.com",
                category = "Trabalho",
                password = " senha super secreta "
            ),
            repository.createdPassword
        )
    }

    @Test
    fun givenExplicitTitle_whenInvoked_thenPreservesTrimmedUserValue() = runTest {
        val repository = FakePasswordRepository(passwordCount = 8)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository)
        )

        val result = useCase(
            title = "  GitHub  ",
            login = "",
            category = "",
            password = "abc123"
        )

        assertEquals(CreatePasswordResult.Success, result)
        assertEquals("GitHub", repository.createdPassword?.title)
    }

    @Test
    fun givenRepositoryFailure_whenInvoked_thenReturnsFailure() = runTest {
        val repository = FakePasswordRepository(shouldFailOnCreate = true)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository)
        )

        val result = useCase(
            title = "",
            login = "",
            category = "",
            password = "abc123"
        )

        assertEquals(CreatePasswordResult.Failure, result)
    }

    private class FakePasswordRepository(
        private val passwordCount: Int = 0,
        private val shouldFailOnCreate: Boolean = false
    ) : PasswordRepository {

        var createdPassword: NewPassword? = null

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override suspend fun getPasswordCount(): Int = passwordCount

        override suspend fun createPassword(password: NewPassword): Long {
            if (shouldFailOnCreate) {
                error("repository failure")
            }
            createdPassword = password
            return 1L
        }
    }
}
