package com.inovalou.seucofregerenciadordesenhas.feature.search.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class GlobalSearchUiState(
    val query: String = "",
    val categories: List<GlobalSearchCategoryUiModel> = emptyList(),
    val passwords: List<GlobalSearchPasswordUiModel> = emptyList(),
    val contentState: GlobalSearchContentState = GlobalSearchContentState.AwaitingQuery
)

data class GlobalSearchCategoryUiModel(
    val id: Long,
    val name: String,
    @DrawableRes val iconResId: Int
)

data class GlobalSearchPasswordUiModel(
    val id: Long,
    val title: String,
    val initials: String
)

sealed interface GlobalSearchContentState {
    data object AwaitingQuery : GlobalSearchContentState
    data object Loading : GlobalSearchContentState
    data object Content : GlobalSearchContentState
    data object Empty : GlobalSearchContentState
    data class Error(@StringRes val messageResId: Int) : GlobalSearchContentState
}
