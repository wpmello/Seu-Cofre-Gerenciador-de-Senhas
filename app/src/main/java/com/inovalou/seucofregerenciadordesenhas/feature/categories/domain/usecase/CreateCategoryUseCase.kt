package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(
        name: String,
        iconKey: String?
    ): CreateCategoryResult {
        val normalizedName = name.trim()
        val validation = CreateCategoryValidation(
            nameError = if (normalizedName.isBlank()) {
                CreateCategoryNameError.Blank
            } else {
                null
            },
            iconError = if (iconKey.isNullOrBlank()) {
                CreateCategoryIconError.Missing
            } else {
                null
            }
        )

        if (validation.hasError) {
            return CreateCategoryResult.ValidationError(validation)
        }

        return try {
            categoryRepository.createCategory(
                name = normalizedName,
                iconKey = checkNotNull(iconKey)
            )
            CreateCategoryResult.Success
        } catch (_: Exception) {
            CreateCategoryResult.Failure
        }
    }
}

data class CreateCategoryValidation(
    val nameError: CreateCategoryNameError? = null,
    val iconError: CreateCategoryIconError? = null
) {
    val hasError: Boolean
        get() = nameError != null || iconError != null
}

enum class CreateCategoryNameError {
    Blank
}

enum class CreateCategoryIconError {
    Missing
}

sealed interface CreateCategoryResult {
    data object Success : CreateCategoryResult
    data class ValidationError(
        val validation: CreateCategoryValidation
    ) : CreateCategoryResult
    data object Failure : CreateCategoryResult
}
