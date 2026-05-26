package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneratePasswordTitleUseCaseTest {

    @Test
    fun givenBlankTitleAndNoStoredPasswords_whenInvoked_thenReturnsFirstAutomaticTitle() = runTest {
        val useCase = GeneratePasswordTitleUseCase(
            passwordRepository = FakePasswordRepository(passwordCount = 0)
        )

        assertEquals("App 1", useCase("   "))
    }

    @Test
    fun givenBlankTitleAndExistingPasswords_whenInvoked_thenReturnsNextAutomaticTitle() = runTest {
        val useCase = GeneratePasswordTitleUseCase(
            passwordRepository = FakePasswordRepository(passwordCount = 2)
        )

        assertEquals("App 3", useCase(""))
    }

    @Test
    fun givenExplicitTitle_whenInvoked_thenKeepsTrimmedUserValue() = runTest {
        val useCase = GeneratePasswordTitleUseCase(
            passwordRepository = FakePasswordRepository(passwordCount = 9)
        )

        assertEquals("GitHub", useCase("  GitHub  "))
    }

    private class FakePasswordRepository(
        private val passwordCount: Int
    ) : PasswordRepository {


        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = passwordCount

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false
    }
}
