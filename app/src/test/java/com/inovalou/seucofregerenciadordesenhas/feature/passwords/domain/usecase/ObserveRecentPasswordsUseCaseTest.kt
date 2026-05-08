package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveRecentPasswordsUseCaseTest {

    @Test
    fun givenRecentPasswordsAndSecuritySnapshots_whenInvoked_thenReturnsRecentPasswordsWithSecurityRisk() = runTest {
        val useCase = ObserveRecentPasswordsUseCase(
            repository = FakePasswordRepository(
                passwords = listOf(
                    passwordSummary(id = 1L, title = "Weak", createdAt = 10L, updatedAt = 10L),
                    passwordSummary(id = 2L, title = "Safe", createdAt = 20L, updatedAt = 20L)
                ),
                snapshots = listOf(
                    PasswordSecuritySnapshot(passwordId = 1L, password = "123456", fingerprint = "weak"),
                    PasswordSecuritySnapshot(passwordId = 2L, password = "VeryStrongCredential!2026", fingerprint = "safe")
                )
            ),
            evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
        )

        val result = useCase(limit = 4).first()

        assertEquals(listOf("Safe", "Weak"), result.map { it.title })
        assertEquals(
            listOf(PasswordSecurityRiskLevel.Low, PasswordSecurityRiskLevel.High),
            result.map { it.securityRiskLevel }
        )
    }

    private fun passwordSummary(
        id: Long,
        title: String,
        createdAt: Long,
        updatedAt: Long
    ) = PasswordSummary(
        id = id,
        title = title,
        login = "user$id@email.com",
        categoryId = null,
        categoryName = null,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private class FakePasswordRepository(
        private val passwords: List<PasswordSummary>,
        private val snapshots: List<PasswordSecuritySnapshot>
    ) : PasswordRepository {

        override fun observePasswords(): Flow<List<PasswordSummary>> = flowOf(passwords)

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            flowOf(passwords)

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
            flowOf(snapshots)

        override suspend fun getPasswordCount(): Int = passwords.size

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false
    }
}
