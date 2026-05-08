package com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveRecentPasswordsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecurityDetailsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveVaultHomeUseCaseTest {

    @Test
    fun givenPasswordsAndSecuritySnapshots_whenObserved_thenCountsTotalAndWeakPasswordsFromSecurityPolicy() = runTest {
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

        val result = useCase().first()

        assertEquals(3, result.totalPasswords)
        assertEquals(1, result.weakPasswords)
    }

    @Test
    fun givenRecentPasswordsAndSecuritySnapshots_whenObserved_thenMapsRecentSecurityRiskFromSecurityPolicy() = runTest {
        val useCase = buildUseCase(
            passwords = listOf(
                passwordSummary(id = 1L, title = "Weak", createdAt = 10L, updatedAt = 10L),
                passwordSummary(id = 2L, title = "Safe", createdAt = 20L, updatedAt = 20L)
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(passwordId = 1L, password = "123456", fingerprint = "weak"),
                PasswordSecuritySnapshot(passwordId = 2L, password = "VeryStrongCredential!2026", fingerprint = "safe")
            )
        )

        val result = useCase().first()

        assertEquals(listOf("Safe", "Weak"), result.recentPasswords.map { it.title })
        assertEquals(
            listOf(PasswordSecurityRiskLevel.Low, PasswordSecurityRiskLevel.High),
            result.recentPasswords.map { it.securityRiskLevel }
        )
    }

    @Test
    fun givenFourCategories_whenObserved_thenSelectsThreeMostRecentlyModifiedAndShowsOtherCard() = runTest {
        val useCase = buildUseCase(
            categories = listOf(
                category(id = 1L, name = "Social", lastModifiedAt = 10L),
                category(id = 2L, name = "Compras", lastModifiedAt = 40L),
                category(id = 3L, name = "Bancos", lastModifiedAt = 30L),
                category(id = 4L, name = "Trabalho", lastModifiedAt = 20L)
            )
        )

        val result = useCase().first()

        assertEquals(listOf("Compras", "Bancos", "Trabalho"), result.categories.map { it.name })
        assertTrue(result.showOtherCategories)
    }

    @Test
    fun givenFewerThanThreeCategories_whenObserved_thenReturnsOnlyAvailableCategoriesWithoutOtherCard() = runTest {
        val useCase = buildUseCase(
            categories = listOf(
                category(id = 1L, name = "Social", lastModifiedAt = 10L),
                category(id = 2L, name = "Bancos", lastModifiedAt = 20L)
            )
        )

        val result = useCase().first()

        assertEquals(listOf("Bancos", "Social"), result.categories.map { it.name })
        assertFalse(result.showOtherCategories)
    }

    @Test
    fun givenPasswordsWithCreatedAndUpdatedDates_whenObserved_thenReturnsAtMostFourMostRecentPasswords() = runTest {
        val useCase = buildUseCase(
            passwords = listOf(
                passwordSummary(id = 1L, title = "Old", createdAt = 10L, updatedAt = 11L),
                passwordSummary(id = 2L, title = "Updated", createdAt = 20L, updatedAt = 90L),
                passwordSummary(id = 3L, title = "Created", createdAt = 80L, updatedAt = 30L),
                passwordSummary(id = 4L, title = "Newest", createdAt = 100L, updatedAt = 99L),
                passwordSummary(id = 5L, title = "Middle", createdAt = 40L, updatedAt = 50L)
            )
        )

        val result = useCase().first()

        assertEquals(
            listOf("Newest", "Updated", "Created", "Middle"),
            result.recentPasswords.map { it.title }
        )
    }

    private fun buildUseCase(
        categories: List<Category> = emptyList(),
        passwords: List<PasswordSummary> = emptyList(),
        snapshots: List<PasswordSecuritySnapshot> = passwords.map { password ->
            PasswordSecuritySnapshot(
                passwordId = password.id,
                password = "VeryStrongCredential!${password.id}2026",
                fingerprint = "password-${password.id}"
            )
        }
    ): ObserveVaultHomeUseCase {
        val passwordRepository = FakePasswordRepository(
            passwords = passwords,
            snapshots = snapshots
        )
        return ObserveVaultHomeUseCase(
            categoryRepository = FakeCategoryRepository(categories),
            passwordRepository = passwordRepository,
            observeRecentPasswordsUseCase = ObserveRecentPasswordsUseCase(
                repository = passwordRepository,
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
            ),
            observeVaultSecurityDetailsUseCase = ObserveVaultSecurityDetailsUseCase(
                repository = passwordRepository,
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
            )
        )
    }

    private fun category(
        id: Long,
        name: String,
        lastModifiedAt: Long
    ) = Category(
        id = id,
        name = name,
        iconKey = "ic_directory",
        itemCount = 0,
        lastModifiedAt = lastModifiedAt
    )

    private fun passwordSummary(
        id: Long,
        title: String,
        createdAt: Long = 0L,
        updatedAt: Long = 0L
    ) = PasswordSummary(
        id = id,
        title = title,
        login = "user$id@email.com",
        categoryId = null,
        categoryName = null,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private class FakeCategoryRepository(
        private val categories: List<Category>
    ) : CategoryRepository {
        override suspend fun createCategory(name: String, iconKey: String): Long = 0L
        override suspend fun getCategoryById(categoryId: Long): Category? = null
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun touchCategory(categoryId: Long) = Unit
        override suspend fun deleteCategoryById(categoryId: Long) = Unit
        override fun observeCategories(): Flow<List<Category>> = flowOf(categories)
    }

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
