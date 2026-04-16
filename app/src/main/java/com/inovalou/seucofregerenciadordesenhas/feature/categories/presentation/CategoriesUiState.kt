package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.R

data class CategoriesUiState(
    val securitySummary: SecuritySummaryUiModel = SecuritySummaryUiModel(),
    val encryptedIndicator: EncryptedIndicatorUiModel = EncryptedIndicatorUiModel(),
    val currentCategory: HighlightedCategoryUiModel = HighlightedCategoryUiModel(),
    val categoriesState: CategoriesContentUiState = CategoriesContentUiState.Loading
)

data class SecuritySummaryUiModel(
    @StringRes val titleResId: Int = R.string.categories_security_status,
    @StringRes val statusResId: Int = R.string.categories_security_excellent,
    val totalItems: Int = 124
)

data class EncryptedIndicatorUiModel(
    @StringRes val labelResId: Int = R.string.categories_encrypted_indicator,
    val isEncrypted: Boolean = true
)

data class HighlightedCategoryUiModel(
    @StringRes val nameResId: Int = R.string.categories_current_category_name,
    val itemCount: Int = 42,
    @StringRes val badgeResId: Int = R.string.categories_current_badge
)

sealed interface CategoriesContentUiState {
    data object Loading : CategoriesContentUiState
    data object Empty : CategoriesContentUiState
    data class Content(val categories: List<CategoryCardUiModel>) : CategoriesContentUiState
    data class Error(@StringRes val messageResId: Int) : CategoriesContentUiState
}

data class CategoryCardUiModel(
    val id: Long,
    val name: String,
    val itemCount: Int
)
