package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel

data class NewCategoryUiState(
    val name: String = "",
    val availableIcons: List<CategorySelectableIconUiModel> = emptyList(),
    val selectedIconKey: String? = null,
    @StringRes val nameErrorResId: Int? = null,
    @StringRes val iconErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null,
    val isSaving: Boolean = false
)

sealed interface NewCategoryEffect {
    data object NavigateBack : NewCategoryEffect
}
