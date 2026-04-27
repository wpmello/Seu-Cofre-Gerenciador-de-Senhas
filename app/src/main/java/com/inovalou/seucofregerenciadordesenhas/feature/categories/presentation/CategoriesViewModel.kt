package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecuritySummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecuritySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    observeCategoriesUseCase: ObserveCategoriesUseCase,
    observeVaultSecuritySummaryUseCase: ObserveVaultSecuritySummaryUseCase,
    private val categoryIconCatalog: CategoryIconCatalog
) : ViewModel() {

    val uiState = observeCategoriesUseCase()
        .combine(
            observeVaultSecuritySummaryUseCase()
                .catch { emit(VaultSecuritySummary.empty()) }
        ) { categories, securitySummary ->
            toUiState(categories, securitySummary)
        }
        .catch {
            emit(
                CategoriesUiState(
                    categoriesState = CategoriesContentUiState.Error(
                        messageResId = R.string.categories_load_error
                    )
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoriesUiState()
        )

    fun onAction(action: CategoriesAction) {
        when (action) {
            CategoriesAction.OnSearchClick,
            CategoriesAction.OnViewAllClick -> Unit
        }
    }

    private fun toUiState(
        categories: List<Category>,
        securitySummary: VaultSecuritySummary
    ): CategoriesUiState {
        val shouldShowBottomViewAllButton = categories.size > 4
        val categoriesState = if (categories.isEmpty()) {
            CategoriesContentUiState.Empty
        } else {
            CategoriesContentUiState.Content(
                categories = categories.take(4).map { category ->
                    CategoryCardUiModel(
                        id = category.id,
                        name = category.name,
                        iconKey = category.iconKey,
                        iconResId = categoryIconCatalog.resolve(category.iconKey).drawableResId,
                        itemCount = category.itemCount
                    )
                }
            )
        }

        return CategoriesUiState(
            securitySummary = securitySummary.toCategoriesSecuritySummaryUiModel(),
            currentCategory = categories.maxWithOrNull(
                compareBy<Category>({ it.lastModifiedAt }, { it.id })
            )?.let { category ->
                HighlightedCategoryUiModel(
                    id = category.id,
                    name = category.name,
                    itemCount = category.itemCount
                )
            },
            categoriesState = categoriesState,
            shouldShowBottomViewAllButton = shouldShowBottomViewAllButton
        )
    }
}
