package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.testing.RecordingDispatcher
import com.inovalou.seucofregerenciadordesenhas.core.testing.testAppDispatchers
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

class ObservePasswordsByCategoryUseCaseTest {

    @Test
    fun givenCategoryPasswordsWithSecuritySnapshots_whenInvoked_thenReturnsPasswordsWithSecurityRisk() =
        runTest {
            val defaultDispatcher = RecordingDispatcher()
            val useCase = ObservePasswordsByCategoryUseCase(
                repository = FakePasswordRepository(
                    passwords = listOf(
                        PasswordSummary(
                            id = 3L,
                            title = "Instagram",
                            login = "@joao_viajante",
                            categoryId = 4L,
                            categoryName = "Social"
                        )
                    ),
                    snapshots = listOf(
                        PasswordSecuritySnapshot(
                            passwordId = 3L,
                            password = "VeryStrongCredential!2026",
                            fingerprint = "instagram"
                        )
                    )
                ),
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase(),
                dispatchers = testAppDispatchers(defaultDispatcher)
            )

            val result = useCase(categoryId = 4L).first()

            assertEquals(4L, result.single().categoryId)
            assertEquals(PasswordSecurityRiskLevel.Low, result.single().securityRiskLevel)
            assertEquals(1, defaultDispatcher.dispatchCount)
        }

    @Test
    fun givenCategoryPasswordWithoutSecuritySnapshot_whenInvoked_thenKeepsHighRiskFallback() =
        runTest {
            val useCase = ObservePasswordsByCategoryUseCase(
                repository = FakePasswordRepository(
                    passwords = listOf(
                        PasswordSummary(
                            id = 9L,
                            title = "Legacy",
                            login = "legacy@email.com",
                            categoryId = 7L,
                            categoryName = "Legacy"
                        )
                    ),
                    snapshots = emptyList()
                ),
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase(),
                dispatchers = testAppDispatchers()
            )

            val result = useCase(categoryId = 7L).first()

            assertEquals(PasswordSecurityRiskLevel.High, result.single().securityRiskLevel)
        }

    private class FakePasswordRepository(
        private val passwords: List<PasswordSummary>,
        private val snapshots: List<PasswordSecuritySnapshot>
    ) : PasswordRepository {


        var lastObservedCategoryId: Long? = null

        override fun observePasswords(): Flow<List<PasswordSummary>> = flowOf(passwords)

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> {
            lastObservedCategoryId = categoryId
            return flowOf(passwords.filter { password -> password.categoryId == categoryId })
        }

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

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false
    }
}
