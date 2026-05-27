package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.securitydetails

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.core.testing.testAppDispatchers
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecurityDetailsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenViewModelCreated_whenStateIsNotObserved_thenStartsLoading() {
        val viewModel = buildViewModel()

        assertTrue(viewModel.uiState.value.contentState is SecurityDetailsContentState.Loading)
    }

    @Test
    fun givenNoPasswords_whenObserved_thenShowsEmptyState() = runTest {
        val viewModel = buildViewModel(passwords = emptyList(), snapshots = emptyList())
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is SecurityDetailsContentState.Empty)
        assertEquals(0, state.totalPasswords)
        assertEquals(100, state.overallScorePercent)
        assertTrue(state.visiblePasswords.isEmpty())
    }

    @Test
    fun givenSecurityDetails_whenObserved_thenMapsSummaryTabsAndDefaultWeakList() = runTest {
        val viewModel = buildViewModel(
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
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is SecurityDetailsContentState.Content)
        assertEquals(53, state.overallScorePercent)
        assertEquals(R.string.categories_security_moderate, state.statusResId)
        assertEquals(4, state.totalPasswords)
        assertEquals(SecurityDetailsTab.Weak, state.selectedTab)
        assertEquals(
            listOf(2, 1, 1),
            state.tabs.map { tab -> tab.count }
        )
        assertEquals(listOf("Sequencia", "Curta mista"), state.visiblePasswords.map { password -> password.title })
        assertEquals(
            listOf("u***@e***.c***", "u***@e***.c***"),
            state.visiblePasswords.map { password -> password.supportingText }
        )
    }

    @Test
    fun givenTabSelected_whenActionIsHandled_thenVisiblePasswordsComeFromSelectedBucket() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                password(id = 1L, title = "Sequencia"),
                password(id = 4L, title = "Segura")
            ),
            snapshots = listOf(
                PasswordSecuritySnapshot(passwordId = 1L, password = "123456", fingerprint = "fp-1"),
                PasswordSecuritySnapshot(passwordId = 4L, password = "S7!mQ2#vN9@tL4\$z", fingerprint = "fp-4")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(SecurityDetailsAction.OnTabSelected(SecurityDetailsTab.Safe))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SecurityDetailsTab.Safe, state.selectedTab)
        assertEquals(listOf("Segura"), state.visiblePasswords.map { password -> password.title })
    }

    @Test
    fun givenBackClick_whenActionIsHandled_thenEmitsNavigateBackEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(SecurityDetailsAction.OnBackClick)

        assertEquals(SecurityDetailsEffect.NavigateBack, effect.await())
    }

    @Test
    fun givenPasswordClick_whenActionIsHandled_thenEmitsOpenPasswordEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(SecurityDetailsAction.OnPasswordClick(passwordId = 42L))

        assertEquals(SecurityDetailsEffect.OpenPassword(passwordId = 42L), effect.await())
    }

    @Test
    fun givenRepositoryFails_whenObserved_thenShowsErrorState() = runTest {
        val repository = object : PasswordRepository {

            override fun observePasswords(): Flow<List<PasswordSummary>> = flow {
                throw IllegalStateException("repository failure")
            }

            override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
                flowOf(emptyList())

            override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
                flowOf(emptyList())

            override suspend fun getPasswordCount(): Int = 0

            override suspend fun createPassword(password: NewPassword): Long = 0L

            override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

            override suspend fun updatePassword(password: PasswordDetails) = Unit

            override suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long?): Boolean =
                false

            override suspend fun deletePasswordById(passwordId: Long): Boolean = false
        }
        val viewModel = SecurityDetailsViewModel(
            observeVaultSecurityDetailsUseCase = ObserveVaultSecurityDetailsUseCase(
                repository = repository,
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase(),
                dispatchers = testAppDispatchers(mainDispatcherRule.dispatcher)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is SecurityDetailsContentState.Error)
    }

    private fun buildViewModel(
        passwords: List<PasswordSummary> = listOf(password(id = 1L, title = "Segura")),
        snapshots: List<PasswordSecuritySnapshot> = listOf(
            PasswordSecuritySnapshot(
                passwordId = 1L,
                password = "S7!mQ2#vN9@tL4\$z",
                fingerprint = "fp-1"
            )
        )
    ): SecurityDetailsViewModel = SecurityDetailsViewModel(
        observeVaultSecurityDetailsUseCase = ObserveVaultSecurityDetailsUseCase(
            repository = FakePasswordRepository(
                passwords = passwords,
                snapshots = snapshots
            ),
            evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase(),
            dispatchers = testAppDispatchers(mainDispatcherRule.dispatcher)
        )
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

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false
    }
}
