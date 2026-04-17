package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(categoryId: Long): DeleteCategoryResult {
        val existingCategory = categoryRepository.getCategoryById(categoryId)
            ?: return DeleteCategoryResult.NotFound

        return try {
            categoryRepository.deleteCategoryById(existingCategory.id)
            DeleteCategoryResult.Success
        } catch (_: Exception) {
            DeleteCategoryResult.Failure
        }
    }
}

sealed interface DeleteCategoryResult {
    data object Success : DeleteCategoryResult
    data object NotFound : DeleteCategoryResult
    data object Failure : DeleteCategoryResult
}
