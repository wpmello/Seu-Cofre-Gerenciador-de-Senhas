package com.inovalou.seucofregerenciadordesenhas.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSearchResult
import com.inovalou.seucofregerenciadordesenhas.feature.search.domain.model.VaultSearchResults
import com.inovalou.seucofregerenciadordesenhas.feature.search.domain.usecase.SearchVaultItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val searchVaultItemsUseCase: SearchVaultItemsUseCase,
    private val categoryIconCatalog: CategoryIconCatalog
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val searchResultState: StateFlow<SearchResultState> = searchQuery
        .map { query -> query.trim() }
        .distinctUntilChanged()
        .debounce(SEARCH_DEBOUNCE_MILLIS)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(SearchResultState.AwaitingQuery)
            } else {
                searchVaultItemsUseCase(query)
                    .map<VaultSearchResults, SearchResultState> { results ->
                        SearchResultState.Results(results)
                    }
                    .onStart { emit(SearchResultState.Loading) }
                    .catch {
                        emit(
                            SearchResultState.Error(
                                messageResId = R.string.global_search_load_error
                            )
                        )
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchResultState.AwaitingQuery
        )

    val uiState: StateFlow<GlobalSearchUiState> = combine(
        searchQuery,
        searchResultState
    ) { query, resultState ->
        resultState.toUiState(query)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GlobalSearchUiState()
        )

    private val _effects = MutableSharedFlow<GlobalSearchEffect>()
    val effects: SharedFlow<GlobalSearchEffect> = _effects.asSharedFlow()

    fun onAction(action: GlobalSearchAction) {
        when (action) {
            is GlobalSearchAction.OnQueryChanged -> {
                searchQuery.value = action.query
            }

            is GlobalSearchAction.OnCategoryClick -> emitEffect(
                GlobalSearchEffect.OpenCategoryDetails(action.categoryId)
            )

            is GlobalSearchAction.OnPasswordClick -> emitEffect(
                GlobalSearchEffect.OpenPasswordDetails(action.passwordId)
            )
        }
    }

    private fun emitEffect(effect: GlobalSearchEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }

    private fun SearchResultState.toUiState(query: String): GlobalSearchUiState =
        when (this) {
            SearchResultState.AwaitingQuery -> GlobalSearchUiState(
                query = query,
                contentState = GlobalSearchContentState.AwaitingQuery
            )

            SearchResultState.Loading -> GlobalSearchUiState(
                query = query,
                contentState = GlobalSearchContentState.Loading
            )

            is SearchResultState.Results -> {
                val categories = results.categories.map { category -> category.toUiModel() }
                val passwords = results.passwords.map { password -> password.toUiModel() }
                GlobalSearchUiState(
                    query = query,
                    categories = categories,
                    passwords = passwords,
                    contentState = if (categories.isEmpty() && passwords.isEmpty()) {
                        GlobalSearchContentState.Empty
                    } else {
                        GlobalSearchContentState.Content
                    }
                )
            }

            is SearchResultState.Error -> GlobalSearchUiState(
                query = query,
                contentState = GlobalSearchContentState.Error(messageResId)
            )
        }

    private fun Category.toUiModel(): GlobalSearchCategoryUiModel =
        GlobalSearchCategoryUiModel(
            id = id,
            name = name,
            iconResId = categoryIconCatalog.resolve(iconKey).drawableResId
        )

    private fun PasswordSearchResult.toUiModel(): GlobalSearchPasswordUiModel =
        GlobalSearchPasswordUiModel(
            id = id,
            title = title,
            initials = title.toInitials()
        )

    private fun String.toInitials(): String {
        val parts = trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        if (parts.isEmpty()) {
            return "A"
        }

        if (parts.size == 1) {
            return parts.first().take(1).uppercase()
        }

        return buildString {
            append(parts.first().take(1))
            append(parts.last().take(1))
        }.uppercase()
    }

    companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 350L
    }
}

private sealed interface SearchResultState {
    data object AwaitingQuery : SearchResultState
    data object Loading : SearchResultState
    data class Results(val results: VaultSearchResults) : SearchResultState
    data class Error(@androidx.annotation.StringRes val messageResId: Int) : SearchResultState
}
