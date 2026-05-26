package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.testing.testAppDispatchers
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityStatus
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecuritySummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveVaultSecuritySummaryUseCaseTest {

    private val evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()

    @Test
    fun givenNoPasswords_whenObservingVaultSecurity_thenReturnsExcellentEmptySummary() = runTest {
        val useCase = ObserveVaultSecuritySummaryUseCase(
            repository = FakePasswordRepository(emptyList()),
            evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase,
            dispatchers = testAppDispatchers()
        )

        val result = useCase().first()

        assertEquals(VaultSecuritySummary.empty(), result)
    }

    @Test
    fun givenAverageBetweenZeroAndTwentyFive_whenObservingVaultSecurity_thenReturnsPoorStatus() = runTest {
        val useCase = ObserveVaultSecuritySummaryUseCase(
            repository = FakePasswordRepository(
                listOf(
                    PasswordSecuritySnapshot(password = "123456", fingerprint = "fp-1"),
                    PasswordSecuritySnapshot(password = "password", fingerprint = "fp-2")
                )
            ),
            evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase,
            dispatchers = testAppDispatchers()
        )

        val result = useCase().first()

        assertEquals(2, result.totalPasswords)
        assertEquals(15, result.averageScorePercent)
        assertEquals(VaultSecurityStatus.Poor, result.status)
    }

    @Test
    fun givenAverageBelowFifty_whenObservingVaultSecurity_thenReturnsPoorStatus() = runTest {
        val useCase = ObserveVaultSecuritySummaryUseCase(
            repository = FakePasswordRepository(
                listOf(
                    PasswordSecuritySnapshot(password = "Ab1!cd2@", fingerprint = "fp-1"),
                    PasswordSecuritySnapshot(password = "Ab1!cd2@", fingerprint = "fp-2")
                )
            ),
            evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase,
            dispatchers = testAppDispatchers()
        )

        val result = useCase().first()

        assertEquals(2, result.totalPasswords)
        assertEquals(35, result.averageScorePercent)
        assertEquals(VaultSecurityStatus.Poor, result.status)
    }

    @Test
    fun givenAverageBetweenFiftyAndNinety_whenObservingVaultSecurity_thenReturnsModerateStatus() = runTest {
        val useCase = ObserveVaultSecuritySummaryUseCase(
            repository = FakePasswordRepository(
                listOf(
                    PasswordSecuritySnapshot(password = "R8!kLm2@Qp7#", fingerprint = "fp-1"),
                    PasswordSecuritySnapshot(password = "Ab1!cd2@", fingerprint = "fp-2")
                )
            ),
            evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase,
            dispatchers = testAppDispatchers()
        )

        val result = useCase().first()

        assertEquals(2, result.totalPasswords)
        assertEquals(58, result.averageScorePercent)
        assertEquals(VaultSecurityStatus.Moderate, result.status)
    }

    @Test
    fun givenAverageAboveNinety_whenObservingVaultSecurity_thenReturnsExcellentStatus() = runTest {
        val useCase = ObserveVaultSecuritySummaryUseCase(
            repository = FakePasswordRepository(
                listOf(
                    PasswordSecuritySnapshot(password = "S7!mQ2#vN9@tL4\$z", fingerprint = "fp-1"),
                    PasswordSecuritySnapshot(password = "S7!mQ2#vN9@tL4\$z", fingerprint = "fp-2")
                )
            ),
            evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase,
            dispatchers = testAppDispatchers()
        )

        val result = useCase().first()

        assertEquals(2, result.totalPasswords)
        assertEquals(100, result.averageScorePercent)
        assertEquals(VaultSecurityStatus.Excellent, result.status)
    }

    @Test
    fun givenDuplicateFingerprints_whenObservingVaultSecurity_thenUsesDuplicatePenaltyInAverage() = runTest {
        val useCase = ObserveVaultSecuritySummaryUseCase(
            repository = FakePasswordRepository(
                listOf(
                    PasswordSecuritySnapshot(password = "S7!mQ2#vN9@tL4\$z", fingerprint = "shared"),
                    PasswordSecuritySnapshot(password = "S7!mQ2#vN9@tL4\$z", fingerprint = "shared")
                )
            ),
            evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase,
            dispatchers = testAppDispatchers()
        )

        val result = useCase().first()

        assertEquals(80, result.averageScorePercent)
        assertEquals(VaultSecurityStatus.Moderate, result.status)
    }

    private class FakePasswordRepository(
        snapshots: List<PasswordSecuritySnapshot>
    ) : PasswordRepository {


        private val snapshotsFlow = MutableStateFlow(snapshots)

        override fun observePasswords(): Flow<List<PasswordSummary>> = flowOf(emptyList())

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            flowOf(emptyList())

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
            snapshotsFlow

        override suspend fun getPasswordCount(): Int = snapshotsFlow.value.size

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long?): Boolean =
            false
    }
}
