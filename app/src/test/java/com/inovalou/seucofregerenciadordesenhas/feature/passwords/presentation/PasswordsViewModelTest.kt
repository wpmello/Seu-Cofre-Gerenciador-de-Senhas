package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObservePasswordsUseCase
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
class PasswordsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenViewModelCreated_whenStateIsObserved_thenStartsInLoadingState() {
        val viewModel = buildViewModel()

        assertTrue(viewModel.uiState.value.contentState is PasswordsContentState.Loading)
    }

    @Test
    fun givenNoPersistedPasswords_whenObservingState_thenShowsEmptyPasswordsState() = runTest {
        val viewModel = buildViewModel(passwords = emptyList())
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is PasswordsContentState.EmptyPasswords)
        assertTrue(state.allPasswords.isEmpty())
        assertTrue(state.filteredPasswords.isEmpty())
        assertEquals(0, state.totalPasswords)
    }

    @Test
    fun givenPersistedPasswords_whenObservingState_thenShowsFullListFromRepository() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Netflix", login = "joao@email.com", categoryId = 1L, categoryName = "Streaming"),
                PasswordSummary(id = 2L, title = "GitHub", login = "jsilva_dev", categoryId = 2L, categoryName = "Work")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is PasswordsContentState.Content)
        assertEquals(2, state.allPasswords.size)
        assertEquals(2, state.filteredPasswords.size)
        assertEquals("Netflix", state.filteredPasswords.first().title)
        assertEquals("j***@e***.c***", state.filteredPasswords.first().supportingText)
        assertEquals("N", state.filteredPasswords.first().initials)
        assertEquals(2, state.totalPasswords)
    }

    @Test
    fun givenSearchQueryMatchesPlainLogin_whenQueryChanges_thenShowsEmptySearchResult() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 2L, title = "GitHub", login = "jsilva_dev", categoryId = 2L, categoryName = "Work")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(PasswordsAction.OnSearchQueryChanged("jsilva"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("jsilva", state.query)
        assertTrue(state.contentState is PasswordsContentState.EmptySearchResult)
        assertTrue(state.filteredPasswords.isEmpty())
    }

    @Test
    fun givenSearchQueryMatchesEmailLogin_whenQueryChanges_thenShowsEmptySearchResult() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Netflix", login = "joao@email.com", categoryId = 1L, categoryName = "Streaming")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(PasswordsAction.OnSearchQueryChanged("joao@email"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("joao@email", state.query)
        assertTrue(state.contentState is PasswordsContentState.EmptySearchResult)
        assertTrue(state.filteredPasswords.isEmpty())
    }

    @Test
    fun givenSearchQueryMatchesPasswordTitle_whenQueryChanges_thenFiltersListByTitleWithoutExposingRawLogin() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Netflix", login = "joao@email.com", categoryId = 1L, categoryName = "Streaming"),
                PasswordSummary(id = 2L, title = "GitHub", login = "jsilva_dev", categoryId = 2L, categoryName = "Work")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(PasswordsAction.OnSearchQueryChanged("github"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("github", state.query)
        assertTrue(state.contentState is PasswordsContentState.Content)
        assertEquals(listOf("GitHub"), state.filteredPasswords.map { it.title })
        assertEquals(listOf("j***"), state.filteredPasswords.map { it.supportingText })
    }

    @Test
    fun givenSearchQueryCleared_whenQueryBecomesBlank_thenRestoresFullList() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Social", login = "@social", categoryId = 1L, categoryName = "Social"),
                PasswordSummary(id = 2L, title = "Privado", login = "user@email.com", categoryId = null, categoryName = "Private")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(PasswordsAction.OnSearchQueryChanged("pri"))
        advanceUntilIdle()
        viewModel.onAction(PasswordsAction.OnSearchQueryChanged(""))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is PasswordsContentState.Content)
        assertEquals(2, state.filteredPasswords.size)
    }

    @Test
    fun givenPasswordWithoutLoginAndWithCategory_whenObservingState_thenDoesNotUseCategoryAsSupportingText() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(
                    id = 3L,
                    title = "Banco",
                    login = "",
                    categoryId = 7L,
                    categoryName = "Financeiro"
                )
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val password = viewModel.uiState.value.filteredPasswords.single()
        assertEquals("", password.supportingText)
    }

    @Test
    fun givenSearchQueryHasNoMatches_whenQueryChanges_thenShowsSearchEmptyState() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Spotify", login = "premium_family_admin", categoryId = 3L, categoryName = "Music")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(PasswordsAction.OnSearchQueryChanged("xyz"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is PasswordsContentState.EmptySearchResult)
        assertTrue(state.filteredPasswords.isEmpty())
    }

    @Test
    fun givenTitleWithMultipleWords_whenObservingState_thenExposesInitialsForListAvatar() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 7L, title = "Google Drive", login = "legacy@login", categoryId = 2L, categoryName = "Work")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertEquals("GD", viewModel.uiState.value.filteredPasswords.single().initials)
    }

    @Test
    fun givenPasswordSecuritySnapshots_whenObservingState_thenExposesListSecurityLevels() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Spotify", login = "premium_family_admin", categoryId = 3L, categoryName = "Music"),
                PasswordSummary(id = 2L, title = "GitHub", login = "jsilva_dev", categoryId = 2L, categoryName = "Work"),
                PasswordSummary(id = 3L, title = "Netflix", login = "joao@email.com", categoryId = 1L, categoryName = "Streaming")
            ),
            securitySnapshots = listOf(
                PasswordSecuritySnapshot(passwordId = 1L, password = "123456", fingerprint = "spotify"),
                PasswordSecuritySnapshot(passwordId = 2L, password = "Qr7!Lp2@Mz9#", fingerprint = "github"),
                PasswordSecuritySnapshot(passwordId = 3L, password = "VeryStrongCredential!2026", fingerprint = "netflix")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertEquals(
            listOf(
                PasswordListItemSecurityLevel.Weak,
                PasswordListItemSecurityLevel.Moderate,
                PasswordListItemSecurityLevel.Safe
            ),
            viewModel.uiState.value.filteredPasswords.map { it.securityLevel }
        )
    }

    @Test
    fun givenPasswordItemClicked_whenActionIsHandled_thenEmitsOpenDetailsEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(PasswordsAction.OnPasswordClick(passwordId = 25L))

        assertEquals(
            PasswordsEffect.OpenPasswordDetails(passwordId = 25L),
            effect.await()
        )
    }

    @Test
    fun givenRepositoryFails_whenObservingState_thenShowsErrorState() = runTest {
        val repository = object : PasswordRepository {

            override fun observePasswords(): Flow<List<PasswordSummary>> = flow {
                throw IllegalStateException("repository failure")
            }

            override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
                flow { emit(emptyList()) }

            override suspend fun getPasswordCount(): Int = 0

            override suspend fun createPassword(password: NewPassword): Long = 0L

            override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

            override suspend fun updatePassword(password: PasswordDetails) = Unit

            override suspend fun hasPasswordDuplicate(
                password: String,
                excludePasswordId: Long?
            ): Boolean = false

            override suspend fun deletePasswordById(passwordId: Long): Boolean = false
        }
        val viewModel = PasswordsViewModel(
            observePasswordsUseCase = ObservePasswordsUseCase(
                repository = repository,
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is PasswordsContentState.Error)
    }

    @Test
    fun givenRepositoryEmitsNewPassword_whenObserved_thenScreenStateReflectsInsertedCredential() = runTest {
        val repository = FakePasswordRepository(emptyList())
        val viewModel = PasswordsViewModel(
            observePasswordsUseCase = ObservePasswordsUseCase(
                repository = repository,
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        repository.emit(
            listOf(
                PasswordSummary(
                    id = 8L,
                    title = "App 1",
                    login = "user@email.com",
                    categoryId = 2L,
                    categoryName = "Work"
                )
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is PasswordsContentState.Content)
        assertEquals(listOf("App 1"), state.filteredPasswords.map { it.title })
    }

    private fun buildViewModel(
        passwords: List<PasswordSummary> = listOf(
            PasswordSummary(id = 1L, title = "Netflix", login = "joao@email.com", categoryId = 1L, categoryName = "Streaming")
        ),
        securitySnapshots: List<PasswordSecuritySnapshot> = passwords.toSafeSecuritySnapshots()
    ): PasswordsViewModel = PasswordsViewModel(
        observePasswordsUseCase = ObservePasswordsUseCase(
            repository = FakePasswordRepository(
                passwords = passwords,
                securitySnapshots = securitySnapshots
            ),
            evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
        )
    )

    private class FakePasswordRepository(
        passwords: List<PasswordSummary>,
        securitySnapshots: List<PasswordSecuritySnapshot> = passwords.toSafeSecuritySnapshots()
    ) : PasswordRepository {


        private val passwordsFlow = MutableStateFlow(passwords)
        private val securitySnapshotsFlow = MutableStateFlow(securitySnapshots)

        override fun observePasswords(): Flow<List<PasswordSummary>> = passwordsFlow

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            passwordsFlow

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
            securitySnapshotsFlow

        override suspend fun getPasswordCount(): Int = passwordsFlow.value.size

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false

        fun emit(
            passwords: List<PasswordSummary>,
            securitySnapshots: List<PasswordSecuritySnapshot> = passwords.toSafeSecuritySnapshots()
        ) {
            passwordsFlow.value = passwords
            securitySnapshotsFlow.value = securitySnapshots
        }
    }
}

private fun List<PasswordSummary>.toSafeSecuritySnapshots(): List<PasswordSecuritySnapshot> =
    map { password ->
        PasswordSecuritySnapshot(
            passwordId = password.id,
            password = "VeryStrongCredential!${password.id}2026",
            fingerprint = "password-${password.id}"
        )
    }
