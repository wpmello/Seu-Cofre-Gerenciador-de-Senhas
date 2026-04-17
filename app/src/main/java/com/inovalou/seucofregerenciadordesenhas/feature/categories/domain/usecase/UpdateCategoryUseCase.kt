package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(
        categoryId: Long,
        name: String,
        iconKey: String?
    ): UpdateCategoryResult {
        val normalizedName = name.trim()
        val validation = UpdateCategoryValidation(
            nameError = if (normalizedName.isBlank()) {
                UpdateCategoryNameError.Blank
            } else {
                null
            },
            iconError = if (iconKey.isNullOrBlank()) {
                UpdateCategoryIconError.Missing
            } else {
                null
            }
        )

        if (validation.hasError) {
            return UpdateCategoryResult.ValidationError(validation)
        }

        val currentCategory = categoryRepository.getCategoryById(categoryId)
            ?: return UpdateCategoryResult.NotFound

        return try {
            categoryRepository.updateCategory(
                currentCategory.copy(
                    name = normalizedName,
                    iconKey = checkNotNull(iconKey)
                )
            )
            UpdateCategoryResult.Success
        } catch (_: Exception) {
            UpdateCategoryResult.Failure
        }
    }
}

data class UpdateCategoryValidation(
    val nameError: UpdateCategoryNameError? = null,
    val iconError: UpdateCategoryIconError? = null
) {
    val hasError: Boolean
        get() = nameError != null || iconError != null
}

enum class UpdateCategoryNameError {
    Blank
}

enum class UpdateCategoryIconError {
    Missing
}

sealed interface UpdateCategoryResult {
    data object Success : UpdateCategoryResult
    data class ValidationError(
        val validation: UpdateCategoryValidation
    ) : UpdateCategoryResult
    data object NotFound : UpdateCategoryResult
    data object Failure : UpdateCategoryResult
}
