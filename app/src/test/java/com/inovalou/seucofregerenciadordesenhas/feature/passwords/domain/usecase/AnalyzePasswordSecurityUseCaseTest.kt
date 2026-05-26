package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyzePasswordSecurityUseCaseTest {

    @Test
    fun givenPasswordAndCurrentCredentialId_whenAnalyzed_thenDelegatesDuplicateLookupAndReturnsPolicyResult() = runTest {
        val repository = FakePasswordRepository(isDuplicate = true)
        val useCase = AnalyzePasswordSecurityUseCase(
            passwordRepository = repository,
            evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
        )

        val result = useCase(
            password = "S7!mQ2#vN9@tL4\$z",
            currentPasswordId = 42L
        )

        assertEquals("S7!mQ2#vN9@tL4\$z", repository.lastDuplicateLookupPassword)
        assertEquals(42L, repository.lastDuplicateLookupExcludedId)
        assertTrue(result.tags.contains(com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityTag.Duplicate))
        assertEquals(80, result.scorePercent)
    }

    private class FakePasswordRepository(
        private val isDuplicate: Boolean
    ) : PasswordRepository {


        var lastDuplicateLookupPassword: String? = null
        var lastDuplicateLookupExcludedId: Long? = null

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean {
            lastDuplicateLookupPassword = password
            lastDuplicateLookupExcludedId = excludePasswordId
            return isDuplicate
        }

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false
    }
}
