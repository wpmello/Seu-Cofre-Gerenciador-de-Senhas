package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query(
        """
        SELECT
            passwords.id,
            passwords.title,
            passwords.login,
            COALESCE(categories.name, passwords.category) AS category,
            passwords.category_id,
            passwords.encrypted_password,
            passwords.password_iv,
            passwords.password_cipher_version,
            passwords.icon_key
        FROM passwords
        LEFT JOIN categories ON categories.id = passwords.category_id
        ORDER BY passwords.title COLLATE NOCASE ASC
        """
    )
    fun observePasswords(): Flow<List<PasswordEntity>>

    @Query(
        """
        SELECT
            passwords.id,
            passwords.title,
            passwords.login,
            COALESCE(categories.name, passwords.category) AS category,
            passwords.category_id,
            passwords.encrypted_password,
            passwords.password_iv,
            passwords.password_cipher_version,
            passwords.icon_key
        FROM passwords
        LEFT JOIN categories ON categories.id = passwords.category_id
        WHERE passwords.category_id = :categoryId
        ORDER BY passwords.title COLLATE NOCASE ASC
        """
    )
    fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(password: PasswordEntity): Long

    @Query("SELECT COUNT(*) FROM passwords")
    suspend fun countPasswords(): Int
}
