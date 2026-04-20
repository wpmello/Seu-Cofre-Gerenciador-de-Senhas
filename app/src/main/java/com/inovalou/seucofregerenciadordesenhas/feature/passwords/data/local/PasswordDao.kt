package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query("SELECT * FROM passwords ORDER BY title COLLATE NOCASE ASC")
    fun observePasswords(): Flow<List<PasswordEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(password: PasswordEntity): Long

    @Query("SELECT COUNT(*) FROM passwords")
    suspend fun countPasswords(): Int
}
