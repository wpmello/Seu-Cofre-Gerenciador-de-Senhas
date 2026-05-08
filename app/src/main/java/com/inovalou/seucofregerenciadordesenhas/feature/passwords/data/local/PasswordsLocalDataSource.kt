package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface PasswordsLocalDataSource {

    fun observePasswords(): Flow<List<PasswordEntity>>

    fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>>

    fun observePasswordCount(): Flow<Int> = observePasswords().map { passwords ->
        passwords.size
    }

    fun observeRecentPasswords(limit: Int): Flow<List<PasswordEntity>> =
        observePasswords().map { passwords ->
            passwords.sortedByMostRecentChange().take(limit)
        }

    suspend fun createPassword(password: PasswordEntity): Long

    suspend fun getPasswordById(passwordId: Long): PasswordEntity?

    suspend fun updatePassword(password: PasswordEntity)

    suspend fun getPasswordCount(): Int

    suspend fun countPasswordsWithFingerprint(
        passwordFingerprint: String,
        excludePasswordId: Long?
    ): Int

    suspend fun getPasswordsMissingFingerprint(): List<PasswordEntity>

    suspend fun updatePasswordFingerprint(passwordId: Long, passwordFingerprint: String)
}

class RoomPasswordsLocalDataSource @Inject constructor(
    private val passwordDao: PasswordDao
) : PasswordsLocalDataSource {

    override fun observePasswords(): Flow<List<PasswordEntity>> = passwordDao.observePasswords()

    override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>> =
        passwordDao.observePasswordsByCategoryId(categoryId)

    override fun observePasswordCount(): Flow<Int> = passwordDao.observePasswordCount()

    override fun observeRecentPasswords(limit: Int): Flow<List<PasswordEntity>> =
        passwordDao.observeRecentPasswords(limit)

    override suspend fun createPassword(password: PasswordEntity): Long = passwordDao.insert(password)

    override suspend fun getPasswordById(passwordId: Long): PasswordEntity? =
        passwordDao.getPasswordById(passwordId)

    override suspend fun updatePassword(password: PasswordEntity) {
        passwordDao.update(password)
    }

    override suspend fun getPasswordCount(): Int = passwordDao.countPasswords()

    override suspend fun countPasswordsWithFingerprint(
        passwordFingerprint: String,
        excludePasswordId: Long?
    ): Int = passwordDao.countPasswordsWithFingerprint(passwordFingerprint, excludePasswordId)

    override suspend fun getPasswordsMissingFingerprint(): List<PasswordEntity> =
        passwordDao.getPasswordsMissingFingerprint()

    override suspend fun updatePasswordFingerprint(passwordId: Long, passwordFingerprint: String) {
        passwordDao.updatePasswordFingerprint(passwordId, passwordFingerprint)
    }
}

private fun List<PasswordEntity>.sortedByMostRecentChange(): List<PasswordEntity> =
    sortedWith(
        compareByDescending<PasswordEntity> { password ->
            maxOf(password.createdAt, password.updatedAt)
        }.thenByDescending { password -> password.id }
    )
