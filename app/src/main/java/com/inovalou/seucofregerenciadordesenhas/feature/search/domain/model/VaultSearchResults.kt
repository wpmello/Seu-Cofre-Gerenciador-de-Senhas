package com.inovalou.seucofregerenciadordesenhas.feature.search.domain.model

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSearchResult

data class VaultSearchResults(
    val categories: List<Category>,
    val passwords: List<PasswordSearchResult>
) {
    companion object {
        fun empty(): VaultSearchResults = VaultSearchResults(
            categories = emptyList(),
            passwords = emptyList()
        )
    }
}
