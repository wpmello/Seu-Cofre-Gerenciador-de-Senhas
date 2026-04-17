package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.allcategories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.CategoryCardUiModel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory.EditCategoryOpenedFrom
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AllCategoriesViewModel @Inject constructor(
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val categoryIconCatalog: CategoryIconCatalog
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<AllCategoriesUiState> = combine(
        observeCategoriesUseCase(),
        searchQuery
    ) { categories, query ->
        categories.toUiState(query = query, categoryIconCatalog = categoryIconCatalog)
    }
        .catch {
            emit(
                AllCategoriesUiState(
                    contentState = AllCategoriesContentState.Error(
                        messageResId = R.string.all_categories_load_error
                    )
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AllCategoriesUiState()
        )

    private val _effects = MutableSharedFlow<AllCategoriesEffect>()
    val effects: SharedFlow<AllCategoriesEffect> = _effects.asSharedFlow()

    fun onAction(action: AllCategoriesAction) {
        when (action) {
            is AllCategoriesAction.OnSearchQueryChanged -> {
                searchQuery.value = action.query
            }

            is AllCategoriesAction.OnCategoryClick -> {
                viewModelScope.launch {
                    _effects.emit(
                        AllCategoriesEffect.NavigateToEditCategory(
                            categoryId = action.categoryId,
                            openedFrom = EditCategoryOpenedFrom.AllCategories
                        )
                    )
                }
            }
        }
    }
}

private fun List<Category>.toUiState(
    query: String,
    categoryIconCatalog: CategoryIconCatalog
): AllCategoriesUiState {
    val allCategories = map { category ->
        CategoryCardUiModel(
            id = category.id,
            name = category.name,
            iconKey = category.iconKey,
            iconResId = categoryIconCatalog.resolve(category.iconKey).drawableResId,
            itemCount = category.itemCount
        )
    }
    val normalizedQuery = query.trim()
    val filteredCategories = if (normalizedQuery.isBlank()) {
        allCategories
    } else {
        allCategories.filter { category ->
            category.name.contains(normalizedQuery, ignoreCase = true)
        }
    }

    val contentState = when {
        allCategories.isEmpty() -> AllCategoriesContentState.EmptyCategories
        filteredCategories.isEmpty() -> AllCategoriesContentState.EmptySearchResult
        else -> AllCategoriesContentState.Content
    }

    return AllCategoriesUiState(
        query = query,
        allCategories = allCategories,
        filteredCategories = filteredCategories,
        contentState = contentState
    )
}
