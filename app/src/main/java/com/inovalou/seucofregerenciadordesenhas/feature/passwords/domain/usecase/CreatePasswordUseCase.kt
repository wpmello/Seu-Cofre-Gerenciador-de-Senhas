package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject

class CreatePasswordUseCase @Inject constructor(
    private val passwordRepository: PasswordRepository,
    private val generatePasswordTitleUseCase: GeneratePasswordTitleUseCase
) {

    suspend operator fun invoke(
        title: String,
        login: String,
        category: String,
        password: String
    ): CreatePasswordResult {
        val validation = CreatePasswordValidation(
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
            passwordRepository.createPassword(
                NewPassword(
                    title = generatePasswordTitleUseCase(title),
                    login = login.trim(),
                    category = category.trim(),
                    password = password
                )
            )
            CreatePasswordResult.Success
        } catch (_: Exception) {
            CreatePasswordResult.Failure
        }
    }
}

data class CreatePasswordValidation(
    val passwordError: CreatePasswordPasswordError? = null
) {
    val hasError: Boolean
        get() = passwordError != null
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
