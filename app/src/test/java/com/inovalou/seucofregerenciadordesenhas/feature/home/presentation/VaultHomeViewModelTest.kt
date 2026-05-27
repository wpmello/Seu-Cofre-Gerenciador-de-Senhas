package com.inovalou.seucofregerenciadordesenhas.feature.home.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.core.testing.testAppDispatchers
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconOption
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase.ObserveVaultHomeUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase.ObserveVaultHomeSecurityPasswordsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveRecentPasswordsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecurityDetailsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenViewModelCreated_whenStateIsNotCollected_thenStartsLoading() {
        val viewModel = buildViewModel()

        assertTrue(viewModel.uiState.value.contentState is VaultHomeContentState.Loading)
    }

    @Test
    fun givenEmptyVault_whenObserved_thenEmitsEmptyStateWithZeroTotals() = runTest {
        val viewModel = buildViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is VaultHomeContentState.Empty)
        assertEquals(0, state.totalPasswords)
        assertEquals(0, state.weakPasswords)
        assertEquals(0, state.moderatePasswords)
        assertEquals(0, state.strongPasswords)
        assertTrue(state.categories.isEmpty())
        assertTrue(state.recentPasswords.isEmpty())
    }

    @Test
    fun givenHomeData_whenObserved_thenMapsCategoriesRecentPasswordsAndSecurityCounts() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(1L, "Social", "ic_global", 2, 10L),
                Category(2L, "Compras", "ic_work_bag", 1, 20L),
                Category(3L, "Bancos", "ic_padlock", 3, 30L)
            ),
            passwords = listOf(
                passwordSummary(1L, "Facebook", createdAt = 10L, updatedAt = 20L),
                passwordSummary(2L, "Banco", createdAt = 30L, updatedAt = 40L),
                passwordSummary(3L, "Email", createdAt = 50L, updatedAt = 60L)
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(1L, "123456", "weak"),
                PasswordSecuritySnapshot(2L, "Qr7!Lp2@Mz9#", "moderate"),
                PasswordSecuritySnapshot(3L, "VeryStrongCredential!2026", "safe")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is VaultHomeContentState.Content)
        assertEquals(3, state.totalPasswords)
        assertEquals(1, state.weakPasswords)
        assertEquals(1, state.moderatePasswords)
        assertEquals(1, state.strongPasswords)
        assertEquals(listOf("Bancos", "Compras", "Social"), state.categories.map { it.name })
        assertTrue(state.showOtherCategories)
        assertEquals(listOf("Email", "Banco", "Facebook"), state.recentPasswords.map { it.title })
        assertEquals(
            listOf(
                "u***@e***.c***",
                "u***@e***.c***",
                "u***@e***.c***"
            ),
            state.recentPasswords.map { it.supportingText }
        )
        assertEquals(
            listOf(
                VaultPasswordListSecurityLevel.Safe,
                VaultPasswordListSecurityLevel.Moderate,
                VaultPasswordListSecurityLevel.Weak
            ),
            state.recentPasswords.map { it.securityLevel }
        )
        assertEquals(R.drawable.ic_padlock, state.categories.first().iconResId)
    }

    @Test
    fun givenRepositoryFailure_whenObserved_thenEmitsErrorState() = runTest {
        val viewModel = buildViewModel(
            categoryRepository = object : CategoryRepository {
                override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) = Unit

                override suspend fun transferPasswordsToCategory(
                    sourceCategoryId: Long,
                    targetCategoryId: Long
                ) = Unit

                override suspend fun createCategory(name: String, iconKey: String): Long = 0L
                override suspend fun getCategoryById(categoryId: Long): Category? = null
                override suspend fun updateCategory(category: Category) = Unit
                override suspend fun touchCategory(categoryId: Long) = Unit
                override suspend fun deleteCategoryById(categoryId: Long) = Unit
                override fun observeCategories(): Flow<List<Category>> = flow {
                    throw IllegalStateException("repository failure")
                }
            }
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is VaultHomeContentState.Error)
    }

    @Test
    fun givenCategoryClicked_whenActionHandled_thenEmitsCategoryNavigationEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(VaultHomeAction.OnCategoryClick(12L))

        assertEquals(VaultHomeEffect.NavigateToCategoryDetails(12L), effect.await())
    }

    @Test
    fun givenOtherClicked_whenActionHandled_thenEmitsAllCategoriesNavigationEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(VaultHomeAction.OnOtherCategoriesClick)

        assertEquals(VaultHomeEffect.NavigateToAllCategories, effect.await())
    }

    @Test
    fun givenRecentPasswordClicked_whenActionHandled_thenEmitsPasswordDetailsNavigationEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(VaultHomeAction.OnRecentPasswordClick(34L))

        assertEquals(VaultHomeEffect.NavigateToPasswordDetails(34L), effect.await())
    }

    @Test
    fun givenSecurityTagClicked_whenListLoads_thenShowsSelectedBucketPasswords() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                passwordSummary(1L, "Weak", createdAt = 10L, updatedAt = 10L),
                passwordSummary(2L, "Safe", createdAt = 20L, updatedAt = 20L)
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(1L, "123456", "weak"),
                PasswordSecuritySnapshot(2L, "VeryStrongCredential!2026", "safe")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(VaultHomeAction.OnSecuritySummaryTagClick(VaultHomeSecurityFilter.Weak))
        advanceUntilIdle()

        val summaryState = viewModel.uiState.value.summaryCardState
        require(summaryState is VaultHomeSummaryCardState.Content)
        assertEquals(VaultHomeSecurityFilter.Weak, summaryState.filter)
        assertEquals(listOf("Weak"), summaryState.passwords.map { password -> password.title })
        assertEquals(listOf("u***@e***.c***"), summaryState.passwords.map { password -> password.supportingText })
        assertEquals(PasswordSecurityBucket.Weak, summaryState.passwords.single().bucket)
    }

    @Test
    fun givenSecurityTagWithoutPasswords_whenListLoads_thenShowsEmptySummaryState() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                passwordSummary(1L, "Safe", createdAt = 10L, updatedAt = 10L)
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(1L, "VeryStrongCredential!2026", "safe")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(VaultHomeAction.OnSecuritySummaryTagClick(VaultHomeSecurityFilter.Moderate))
        advanceUntilIdle()

        val summaryState = viewModel.uiState.value.summaryCardState
        require(summaryState is VaultHomeSummaryCardState.Empty)
        assertEquals(VaultHomeSecurityFilter.Moderate, summaryState.filter)
    }

    @Test
    fun givenSecurityListOpen_whenBackClicked_thenSummaryReturnsToOverview() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                passwordSummary(1L, "Weak", createdAt = 10L, updatedAt = 10L)
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(1L, "123456", "weak")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(VaultHomeAction.OnSecuritySummaryTagClick(VaultHomeSecurityFilter.Weak))
        advanceUntilIdle()
        viewModel.onAction(VaultHomeAction.OnSecuritySummaryBackClick)
        advanceUntilIdle()

        assertEquals(VaultHomeSummaryCardState.Overview, viewModel.uiState.value.summaryCardState)
    }

    @Test
    fun givenSecuritySummaryPasswordClicked_whenActionHandled_thenEmitsPasswordDetailsNavigationEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(VaultHomeAction.OnSecuritySummaryPasswordClick(45L))

        assertEquals(VaultHomeEffect.NavigateToPasswordDetails(45L), effect.await())
    }

    @Test
    fun givenSecurityListFailure_whenTagClicked_thenKeepsHomeContentAndShowsSummaryError() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                passwordSummary(1L, "Weak", createdAt = 10L, updatedAt = 10L)
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(1L, "123456", "weak")
            ),
            summaryPasswordRepository = FailingSecurityPasswordRepository(
                passwords = listOf(
                    passwordSummary(1L, "Weak", createdAt = 10L, updatedAt = 10L)
                )
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(VaultHomeAction.OnSecuritySummaryTagClick(VaultHomeSecurityFilter.Weak))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is VaultHomeContentState.Content)
        val summaryState = viewModel.uiState.value.summaryCardState
        require(summaryState is VaultHomeSummaryCardState.Error)
        assertEquals(VaultHomeSecurityFilter.Weak, summaryState.filter)
        assertEquals(R.string.vault_home_summary_passwords_error, summaryState.messageResId)
    }

    @Test
    fun givenViewAllClicked_whenActionHandled_thenEmitsPasswordsNavigationEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(VaultHomeAction.OnViewAllPasswordsClick)

        assertEquals(VaultHomeEffect.NavigateToPasswords, effect.await())
    }

    private fun buildViewModel(
        categories: List<Category> = emptyList(),
        passwords: List<PasswordSummary> = emptyList(),
        snapshots: List<PasswordSecuritySnapshot> = passwords.map { password ->
            PasswordSecuritySnapshot(password.id, "VeryStrongCredential!${password.id}2026", "password-${password.id}")
        },
        categoryRepository: CategoryRepository = FakeCategoryRepository(categories),
        summaryPasswordRepository: PasswordRepository? = null
    ): VaultHomeViewModel {
        val passwordRepository = FakePasswordRepository(passwords, snapshots)
        val resolvedSummaryPasswordRepository = summaryPasswordRepository ?: passwordRepository
        return VaultHomeViewModel(
            observeVaultHomeUseCase = ObserveVaultHomeUseCase(
                categoryRepository = categoryRepository,
                passwordRepository = passwordRepository,
                observeRecentPasswordsUseCase = ObserveRecentPasswordsUseCase(
                    repository = passwordRepository,
                    evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase(),
                    dispatchers = testAppDispatchers(mainDispatcherRule.dispatcher)
                ),
                observeVaultSecurityDetailsUseCase = ObserveVaultSecurityDetailsUseCase(
                    repository = passwordRepository,
                    evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase(),
                    dispatchers = testAppDispatchers(mainDispatcherRule.dispatcher)
                )
            ),
            observeVaultHomeSecurityPasswordsUseCase = ObserveVaultHomeSecurityPasswordsUseCase(
                observeVaultSecurityDetailsUseCase = ObserveVaultSecurityDetailsUseCase(
                    repository = resolvedSummaryPasswordRepository,
                    evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase(),
                    dispatchers = testAppDispatchers(mainDispatcherRule.dispatcher)
                )
            ),
            categoryIconCatalog = FakeCategoryIconCatalog()
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

    private class FakeCategoryRepository(
        categories: List<Category>
    ) : CategoryRepository {
        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) = Unit

        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long
        ) = Unit

        private val categoriesFlow = MutableStateFlow(categories)
        override suspend fun createCategory(name: String, iconKey: String): Long = 0L
        override suspend fun getCategoryById(categoryId: Long): Category? = null
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun touchCategory(categoryId: Long) = Unit
        override suspend fun deleteCategoryById(categoryId: Long) = Unit
        override fun observeCategories(): Flow<List<Category>> = categoriesFlow
    }

    private class FakePasswordRepository(
        private val passwords: List<PasswordSummary>,
        private val snapshots: List<PasswordSecuritySnapshot>
    ) : PasswordRepository {

        override fun observePasswords(): Flow<List<PasswordSummary>> = MutableStateFlow(passwords)
        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            MutableStateFlow(passwords)

        override fun observePasswordCount(): Flow<Int> = MutableStateFlow(passwords.size)
        override fun observeRecentPasswords(limit: Int): Flow<List<PasswordSummary>> =
            MutableStateFlow(passwords)

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
            MutableStateFlow(snapshots)

        override suspend fun getPasswordCount(): Int = passwords.size
        override suspend fun createPassword(password: NewPassword): Long = 0L
        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null
        override suspend fun updatePassword(password: PasswordDetails) = Unit
        override suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long?): Boolean =
            false

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false
    }

    private class FailingSecurityPasswordRepository(
        private val passwords: List<PasswordSummary>
    ) : PasswordRepository {

        override fun observePasswords(): Flow<List<PasswordSummary>> = MutableStateFlow(passwords)
        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            MutableStateFlow(passwords)

        override fun observePasswordCount(): Flow<Int> = MutableStateFlow(passwords.size)
        override fun observeRecentPasswords(limit: Int): Flow<List<PasswordSummary>> =
            MutableStateFlow(passwords)

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> = flow {
            throw IllegalStateException("summary failure")
        }

        override suspend fun getPasswordCount(): Int = passwords.size
        override suspend fun createPassword(password: NewPassword): Long = 0L
        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null
        override suspend fun updatePassword(password: PasswordDetails) = Unit
        override suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long?): Boolean =
            false

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false
    }

    private class FakeCategoryIconCatalog : CategoryIconCatalog {
        override fun all(): List<CategoryIconOption> = emptyList()
        override fun resolve(iconKey: String): CategoryIconOption = when (iconKey) {
            "ic_global" -> CategoryIconOption(iconKey, R.drawable.ic_global)
            "ic_work_bag" -> CategoryIconOption(iconKey, R.drawable.ic_work_bag)
            "ic_padlock" -> CategoryIconOption(iconKey, R.drawable.ic_padlock)
            else -> CategoryIconOption(iconKey, R.drawable.ic_directory)
        }

        override fun default(): CategoryIconOption = CategoryIconOption("ic_directory", R.drawable.ic_directory)
    }
}
