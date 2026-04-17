package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class NewCategoryUiState(
    val name: String = "",
    val availableIcons: List<NewCategoryIconUiModel> = emptyList(),
    val selectedIconKey: String? = null,
    @StringRes val nameErrorResId: Int? = null,
    @StringRes val iconErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null,
    val isSaving: Boolean = false
)

data class NewCategoryIconUiModel(
    val iconKey: String,
    @DrawableRes val iconResId: Int,
    val isSelected: Boolean
)

sealed interface NewCategoryEffect {
    data object NavigateBack : NewCategoryEffect
}
