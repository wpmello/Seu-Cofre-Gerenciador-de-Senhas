package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject

class CreatePasswordUseCase @Inject constructor(
    private val passwordRepository: PasswordRepository,
    private val categoryRepository: CategoryRepository,
    private val generatePasswordTitleUseCase: GeneratePasswordTitleUseCase,
    private val timeProvider: TimeProvider
) {

    suspend operator fun invoke(
        title: String,
        login: String,
        categoryId: Long?,
        categoryName: String?,
        password: String
    ): CreatePasswordResult {
        val persistedCategory = when {
            categoryId == null -> null
            else -> categoryRepository.getCategoryById(categoryId)
        }
        val validation = CreatePasswordValidation(
            categoryError = when {
                categoryId == null -> CreatePasswordCategoryError.Missing
                persistedCategory == null -> CreatePasswordCategoryError.Invalid
                else -> null
            },
            passwordError = if (password.isBlank()) {
                CreatePasswordPasswordError.Blank
            } else {
                null
            }
        )

        if (validation.hasError) {
            return CreatePasswordResult.ValidationError(validation)
        }

        return try {
            val now = timeProvider.currentTimeMillis()
            passwordRepository.createPassword(
                NewPassword(
                    title = generatePasswordTitleUseCase(title),
                    login = login.trim(),
                    categoryId = persistedCategory?.id,
                    categoryName = persistedCategory?.name ?: categoryName?.trim()?.takeIf { it.isNotBlank() },
                    password = password,
                    createdAt = now,
                    updatedAt = now
                )
            )
            persistedCategory?.id?.let { categoryRepository.touchCategory(it) }
            CreatePasswordResult.Success
        } catch (_: Exception) {
            CreatePasswordResult.Failure
        }
    }
}

data class CreatePasswordValidation(
    val categoryError: CreatePasswordCategoryError? = null,
    val passwordError: CreatePasswordPasswordError? = null
) {
    val hasError: Boolean
        get() = categoryError != null || passwordError != null
}

enum class CreatePasswordCategoryError {
    Missing,
    Invalid
}

enum class CreatePasswordPasswordError {
    Blank
}

sealed interface CreatePasswordResult {
    data object Success : CreatePasswordResult
    data class ValidationError(
        val validation: CreatePasswordValidation
    ) : CreatePasswordResult
    data object Failure : CreatePasswordResult
}
