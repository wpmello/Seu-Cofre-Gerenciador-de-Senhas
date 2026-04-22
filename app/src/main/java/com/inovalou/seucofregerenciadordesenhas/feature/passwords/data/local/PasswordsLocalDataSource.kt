package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface PasswordsLocalDataSource {

    fun observePasswords(): Flow<List<PasswordEntity>>

    fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>>

    suspend fun createPassword(password: PasswordEntity): Long

    suspend fun getPasswordCount(): Int
}

class RoomPasswordsLocalDataSource @Inject constructor(
    private val passwordDao: PasswordDao
) : PasswordsLocalDataSource {

    override fun observePasswords(): Flow<List<PasswordEntity>> = passwordDao.observePasswords()

    override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>> =
        passwordDao.observePasswordsByCategoryId(categoryId)

    override suspend fun createPassword(password: PasswordEntity): Long = passwordDao.insert(password)

    override suspend fun getPasswordCount(): Int = passwordDao.countPasswords()
}
