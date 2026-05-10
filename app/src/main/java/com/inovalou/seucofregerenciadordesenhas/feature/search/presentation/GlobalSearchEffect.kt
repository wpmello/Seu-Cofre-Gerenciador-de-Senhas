package com.inovalou.seucofregerenciadordesenhas.feature.search.presentation

sealed interface GlobalSearchEffect {
    data class OpenCategoryDetails(val categoryId: Long) : GlobalSearchEffect
    data class OpenPasswordDetails(val passwordId: Long) : GlobalSearchEffect
}
