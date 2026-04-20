package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface PasswordsLocalDataSource {

    fun observePasswords(): Flow<List<PasswordEntity>>
}

class RoomPasswordsLocalDataSource @Inject constructor(
    private val passwordDao: PasswordDao
) : PasswordsLocalDataSource {

    override fun observePasswords(): Flow<List<PasswordEntity>> = passwordDao.observePasswords()
}
