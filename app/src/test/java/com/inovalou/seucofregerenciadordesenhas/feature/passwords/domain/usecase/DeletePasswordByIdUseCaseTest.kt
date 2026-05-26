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

class DeletePasswordByIdUseCaseTest {

    @Test
    fun givenExistingPassword_whenDeleting_thenDeletesByIdAndReturnsSuccess() = runTest {
        val repository = FakePasswordRepository(deleteResult = true)
        val useCase = DeletePasswordByIdUseCase(repository)

        val result = useCase(passwordId = 8L)

        assertEquals(DeletePasswordResult.Success, result)
        assertEquals(8L, repository.deletedPasswordId)
    }

    @Test
    fun givenMissingPassword_whenDeleting_thenReturnsNotFound() = runTest {
        val repository = FakePasswordRepository(deleteResult = false)
        val useCase = DeletePasswordByIdUseCase(repository)

        val result = useCase(passwordId = 404L)

        assertEquals(DeletePasswordResult.NotFound, result)
        assertEquals(404L, repository.deletedPasswordId)
    }

    @Test
    fun givenRepositoryFailure_whenDeleting_thenReturnsFailureWithoutLeakingSensitiveData() = runTest {
        val repository = FakePasswordRepository(failDelete = true)
        val useCase = DeletePasswordByIdUseCase(repository)

        val result = useCase(passwordId = 8L)

        assertEquals(DeletePasswordResult.Failure, result)
        assertEquals(8L, repository.deletedPasswordId)
    }

    private class FakePasswordRepository(
        private val deleteResult: Boolean = true,
        private val failDelete: Boolean = false
    ) : PasswordRepository {

        var deletedPasswordId: Long? = null

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
        ): Boolean = false

        override suspend fun deletePasswordById(passwordId: Long): Boolean {
            deletedPasswordId = passwordId
            if (failDelete) {
                error("delete failure")
            }
            return deleteResult
        }
    }
}
