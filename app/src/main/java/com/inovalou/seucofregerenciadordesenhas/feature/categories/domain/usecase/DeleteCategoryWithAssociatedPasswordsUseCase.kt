package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryWithAssociatedPasswordsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(categoryId: Long): DeleteCategoryResult {
        categoryRepository.getCategoryById(categoryId)
            ?: return DeleteCategoryResult.NotFound

        return try {
            categoryRepository.deleteCategoryWithAssociatedPasswords(categoryId)
            DeleteCategoryResult.Success
        } catch (_: Exception) {
            DeleteCategoryResult.Failure
        }
    }
}
