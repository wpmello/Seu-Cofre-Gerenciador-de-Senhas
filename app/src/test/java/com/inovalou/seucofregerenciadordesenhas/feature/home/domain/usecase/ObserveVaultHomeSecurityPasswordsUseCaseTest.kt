package com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecurityDetailsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveVaultHomeSecurityPasswordsUseCaseTest {

    @Test
    fun givenSecurityBucketSelected_whenObserved_thenReturnsOnlyMatchingPasswordsFromSecurityPolicy() = runTest {
        val useCase = buildUseCase(
            passwords = listOf(
                passwordSummary(id = 1L, title = "Weak"),
                passwordSummary(id = 2L, title = "Moderate"),
                passwordSummary(id = 3L, title = "Safe")
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(passwordId = 1L, password = "123456", fingerprint = "weak"),
                PasswordSecuritySnapshot(passwordId = 2L, password = "Qr7!Lp2@Mz9#", fingerprint = "moderate"),
                PasswordSecuritySnapshot(passwordId = 3L, password = "VeryStrongCredential!2026", fingerprint = "safe")
            )
        )

        val result = useCase(PasswordSecurityBucket.Weak).first()

        assertEquals(listOf("Weak"), result.map { password -> password.title })
        assertEquals(PasswordSecurityBucket.Weak, result.single().bucket)
        assertTrue(result.single().scorePercent < 50)
    }

    @Test
    fun givenBucketWithoutPasswords_whenObserved_thenReturnsEmptyList() = runTest {
        val useCase = buildUseCase(
            passwords = listOf(
                passwordSummary(id = 1L, title = "Safe")
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(
                    passwordId = 1L,
                    password = "VeryStrongCredential!2026",
                    fingerprint = "safe"
                )
            )
        )

        val result = useCase(PasswordSecurityBucket.Moderate).first()

        assertTrue(result.isEmpty())
    }

    private fun buildUseCase(
        passwords: List<PasswordSummary>,
        snapshots: List<PasswordSecuritySnapshot>
    ): ObserveVaultHomeSecurityPasswordsUseCase {
        val passwordRepository = FakePasswordRepository(
            passwords = passwords,
            snapshots = snapshots
        )
        return ObserveVaultHomeSecurityPasswordsUseCase(
            observeVaultSecurityDetailsUseCase = ObserveVaultSecurityDetailsUseCase(
                repository = passwordRepository,
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
            )
        )
    }

    private fun passwordSummary(
        id: Long,
        title: String
    ) = PasswordSummary(
        id = id,
        title = title,
        login = "user$id@email.com",
        categoryId = null,
        categoryName = null
    )

    private class FakePasswordRepository(
        private val passwords: List<PasswordSummary>,
        private val snapshots: List<PasswordSecuritySnapshot>
    ) : PasswordRepository {
        override fun observePasswords(): Flow<List<PasswordSummary>> = flowOf(passwords)
        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            flowOf(passwords)

        override fun observePasswordCount(): Flow<Int> = flowOf(passwords.size)
        override fun observeRecentPasswords(limit: Int): Flow<List<PasswordSummary>> =
            flowOf(passwords)

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
            flowOf(snapshots)

        override suspend fun getPasswordCount(): Int = passwords.size
        override suspend fun createPassword(password: NewPassword): Long = 0L
        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null
        override suspend fun updatePassword(password: PasswordDetails) = Unit
        override suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long?): Boolean =
            false
    }
}
