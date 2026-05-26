package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.testing.testAppDispatchers
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityTag
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityStatus
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveVaultSecurityDetailsUseCaseTest {

    private val evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()

    @Test
    fun givenNoPasswords_whenObservingDetails_thenReturnsEmptyDetails() = runTest {
        val useCase = buildUseCase(passwords = emptyList(), snapshots = emptyList())

        val result = useCase().first()

        assertEquals(0, result.totalPasswords)
        assertEquals(100, result.averageScorePercent)
        assertEquals(VaultSecurityStatus.Excellent, result.status)
        PasswordSecurityBucket.entries.forEach { bucket ->
            assertTrue(result.passwordsFor(bucket).isEmpty())
        }
    }

    @Test
    fun givenPasswordsAcrossSecurityScores_whenObservingDetails_thenAggregatesAndBucketsThem() = runTest {
        val useCase = buildUseCase(
            passwords = listOf(
                password(id = 1L, title = "Sequencia"),
                password(id = 2L, title = "Curta mista"),
                password(id = 3L, title = "Boa"),
                password(id = 4L, title = "Segura")
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(passwordId = 1L, password = "123456", fingerprint = "fp-1"),
                PasswordSecuritySnapshot(passwordId = 2L, password = "Ab1!cd2@", fingerprint = "fp-2"),
                PasswordSecuritySnapshot(passwordId = 3L, password = "Abcd1234!xyz", fingerprint = "fp-3"),
                PasswordSecuritySnapshot(passwordId = 4L, password = "S7!mQ2#vN9@tL4\$z", fingerprint = "fp-4")
            )
        )

        val result = useCase().first()

        assertEquals(4, result.totalPasswords)
        assertEquals(53, result.averageScorePercent)
        assertEquals(VaultSecurityStatus.Moderate, result.status)
        assertEquals(listOf("Sequencia", "Curta mista"), result.passwordsFor(PasswordSecurityBucket.Weak).map { it.title })
        assertEquals(listOf("Boa"), result.passwordsFor(PasswordSecurityBucket.Moderate).map { it.title })
        assertEquals(listOf("Segura"), result.passwordsFor(PasswordSecurityBucket.Safe).map { it.title })
    }

    @Test
    fun givenDuplicatePasswords_whenObservingDetails_thenUsesDuplicatePenaltyAndTagFromExistingRule() = runTest {
        val useCase = buildUseCase(
            passwords = listOf(
                password(id = 1L, title = "Banco"),
                password(id = 2L, title = "Email")
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(passwordId = 1L, password = "S7!mQ2#vN9@tL4\$z", fingerprint = "shared"),
                PasswordSecuritySnapshot(passwordId = 2L, password = "S7!mQ2#vN9@tL4\$z", fingerprint = "shared")
            )
        )

        val result = useCase().first()

        assertEquals(80, result.averageScorePercent)
        assertEquals(VaultSecurityStatus.Moderate, result.status)
        assertEquals(2, result.countFor(PasswordSecurityBucket.Moderate))
        assertEquals(0, result.countFor(PasswordSecurityBucket.Safe))
        assertTrue(
            result.passwordsFor(PasswordSecurityBucket.Moderate)
                .all { item -> PasswordSecurityTag.Duplicate in item.tags }
        )
    }

    private fun buildUseCase(
        passwords: List<PasswordSummary>,
        snapshots: List<PasswordSecuritySnapshot>
    ): ObserveVaultSecurityDetailsUseCase = ObserveVaultSecurityDetailsUseCase(
        repository = FakePasswordRepository(
            passwords = passwords,
            snapshots = snapshots
        ),
        evaluatePasswordSecurityUseCase = evaluatePasswordSecurityUseCase,
        dispatchers = testAppDispatchers()
    )

    private fun password(id: Long, title: String): PasswordSummary = PasswordSummary(
        id = id,
        title = title,
        login = "usuario$id@email.com",
        categoryId = id,
        categoryName = "Categoria $id"
    )

    private class FakePasswordRepository(
        passwords: List<PasswordSummary>,
        snapshots: List<PasswordSecuritySnapshot>
    ) : PasswordRepository {


        private val passwordsFlow = MutableStateFlow(passwords)
        private val snapshotsFlow = MutableStateFlow(snapshots)

        override fun observePasswords(): Flow<List<PasswordSummary>> = passwordsFlow

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            flowOf(emptyList())

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
            snapshotsFlow

        override suspend fun getPasswordCount(): Int = passwordsFlow.value.size

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long?): Boolean =
            false
    }
}
