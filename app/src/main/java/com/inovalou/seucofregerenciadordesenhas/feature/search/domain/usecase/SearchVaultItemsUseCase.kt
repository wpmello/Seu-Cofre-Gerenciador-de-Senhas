package com.inovalou.seucofregerenciadordesenhas.feature.search.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.search.domain.model.VaultSearchResults
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class SearchVaultItemsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val passwordRepository: PasswordRepository
) {

    operator fun invoke(query: String): Flow<VaultSearchResults> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            return flowOf(VaultSearchResults.empty())
        }

        return combine(
            categoryRepository.observeCategoriesMatchingQuery(normalizedQuery),
            passwordRepository.observePasswordSearchResults(normalizedQuery)
        ) { categories, passwords ->
            VaultSearchResults(
                categories = categories,
                passwords = passwords
            )
        }
    }
}
