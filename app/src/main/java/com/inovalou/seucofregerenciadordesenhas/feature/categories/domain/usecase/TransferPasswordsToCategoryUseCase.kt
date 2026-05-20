package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import javax.inject.Inject

class TransferPasswordsToCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(
        sourceCategoryId: Long,
        targetCategoryId: Long
    ): TransferPasswordsToCategoryResult {
        if (sourceCategoryId == targetCategoryId) {
            return TransferPasswordsToCategoryResult.SameCategory
        }

        categoryRepository.getCategoryById(sourceCategoryId)
            ?: return TransferPasswordsToCategoryResult.SourceNotFound
        categoryRepository.getCategoryById(targetCategoryId)
            ?: return TransferPasswordsToCategoryResult.TargetNotFound

        return try {
            categoryRepository.transferPasswordsToCategory(
                sourceCategoryId = sourceCategoryId,
                targetCategoryId = targetCategoryId
            )
            TransferPasswordsToCategoryResult.Success
        } catch (_: Exception) {
            TransferPasswordsToCategoryResult.Failure
        }
    }
}

sealed interface TransferPasswordsToCategoryResult {
    data object Success : TransferPasswordsToCategoryResult
    data object SourceNotFound : TransferPasswordsToCategoryResult
    data object TargetNotFound : TransferPasswordsToCategoryResult
    data object SameCategory : TransferPasswordsToCategoryResult
    data object Failure : TransferPasswordsToCategoryResult
}
