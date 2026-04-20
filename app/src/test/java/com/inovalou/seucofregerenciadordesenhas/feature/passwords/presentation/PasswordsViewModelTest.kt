package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.core.ui.icon.VaultIconCatalog
import com.inovalou.seucofregerenciadordesenhas.core.ui.icon.VaultIconOption
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
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
                PasswordSummary(id = 1L, title = "Netflix", login = "joao@email.com", iconKey = "ic_home"),
                PasswordSummary(id = 2L, title = "GitHub", login = "jsilva_dev", iconKey = "ic_cloud")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is PasswordsContentState.Content)
        assertEquals(2, state.allPasswords.size)
        assertEquals(2, state.filteredPasswords.size)
        assertEquals("Netflix", state.filteredPasswords.first().title)
        assertEquals(2, state.totalPasswords)
    }

    @Test
    fun givenSearchQueryMatchesPasswords_whenQueryChanges_thenFiltersListByTitleOrLoginIgnoringCase() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Netflix", login = "joao@email.com", iconKey = "ic_home"),
                PasswordSummary(id = 2L, title = "GitHub", login = "jsilva_dev", iconKey = "ic_cloud"),
                PasswordSummary(id = 3L, title = "Workspace", login = "work@empresa.com", iconKey = "ic_directory")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(PasswordsAction.OnSearchQueryChanged("jsilva"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("jsilva", state.query)
        assertEquals(listOf("GitHub"), state.filteredPasswords.map { it.title })
    }

    @Test
    fun givenSearchQueryCleared_whenQueryBecomesBlank_thenRestoresFullList() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Social", login = "@social", iconKey = "ic_global"),
                PasswordSummary(id = 2L, title = "Privado", login = "user@email.com", iconKey = "ic_padlock")
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
    fun givenSearchQueryHasNoMatches_whenQueryChanges_thenShowsSearchEmptyState() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 1L, title = "Spotify", login = "premium_family_admin", iconKey = "ic_favorite")
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
    fun givenUnknownIconKey_whenObservingState_thenUsesSafeFallbackIcon() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(id = 7L, title = "Legado", login = "legacy@login", iconKey = "ic_unknown_legacy")
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertEquals(R.drawable.ic_directory, viewModel.uiState.value.filteredPasswords.single().iconResId)
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
        }
        val viewModel = PasswordsViewModel(
            observePasswordsUseCase = ObservePasswordsUseCase(repository),
            iconCatalog = FakeVaultIconCatalog()
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is PasswordsContentState.Error)
    }

    private fun buildViewModel(
        passwords: List<PasswordSummary> = listOf(
            PasswordSummary(id = 1L, title = "Netflix", login = "joao@email.com", iconKey = "ic_home")
        )
    ): PasswordsViewModel = PasswordsViewModel(
        observePasswordsUseCase = ObservePasswordsUseCase(FakePasswordRepository(passwords)),
        iconCatalog = FakeVaultIconCatalog()
    )

    private class FakePasswordRepository(
        passwords: List<PasswordSummary>
    ) : PasswordRepository {

        private val passwordsFlow = MutableStateFlow(passwords)

        override fun observePasswords(): Flow<List<PasswordSummary>> = passwordsFlow
    }

    private class FakeVaultIconCatalog : VaultIconCatalog {
        private val icons = listOf(
            VaultIconOption("ic_directory", R.drawable.ic_directory),
            VaultIconOption("ic_home", R.drawable.ic_home),
            VaultIconOption("ic_cloud", R.drawable.ic_cloud),
            VaultIconOption("ic_global", R.drawable.ic_global),
            VaultIconOption("ic_padlock", R.drawable.ic_padlock),
            VaultIconOption("ic_favorite", R.drawable.ic_favorite),
            VaultIconOption("ic_user_profile", R.drawable.ic_user_profile)
        )

        override fun all(): List<VaultIconOption> = icons

        override fun resolve(iconKey: String): VaultIconOption =
            icons.firstOrNull { icon -> icon.iconKey == iconKey } ?: icons.first()

        override fun default(): VaultIconOption = icons.first()
    }
}
