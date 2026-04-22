package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.repository

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.crypto.PasswordCipher
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordsLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.mapper.toDomain
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PasswordRepositoryImpl @Inject constructor(
    private val localDataSource: PasswordsLocalDataSource,
    private val passwordCipher: PasswordCipher
) : PasswordRepository {

    override fun observePasswords(): Flow<List<PasswordSummary>> =
        localDataSource.observePasswords().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }

    override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
        localDataSource.observePasswordsByCategoryId(categoryId).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }

    override suspend fun getPasswordCount(): Int = localDataSource.getPasswordCount()

    override suspend fun createPassword(password: NewPassword): Long {
        val encryptedPassword = passwordCipher.encrypt(password.password)
        return localDataSource.createPassword(
            com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity(
                title = password.title,
                login = password.login,
                category = password.categoryName.orEmpty(),
                categoryId = password.categoryId,
                encryptedPassword = encryptedPassword.cipherText,
                passwordIv = encryptedPassword.iv,
                passwordCipherVersion = encryptedPassword.version,
                iconKey = ""
            )
        )
    }
}
