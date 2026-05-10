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
            passwords.icon_key,
            passwords.note,
            passwords.password_fingerprint,
            passwords.created_at,
            passwords.updated_at
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
            passwords.icon_key,
            passwords.note,
            passwords.password_fingerprint,
            passwords.created_at,
            passwords.updated_at
        FROM passwords
        LEFT JOIN categories ON categories.id = passwords.category_id
        WHERE passwords.category_id = :categoryId
        ORDER BY passwords.title COLLATE NOCASE ASC
        """
    )
    fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>>

    @Query("SELECT COUNT(*) FROM passwords")
    fun observePasswordCount(): Flow<Int>

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
            passwords.icon_key,
            passwords.note,
            passwords.password_fingerprint,
            passwords.created_at,
            passwords.updated_at
        FROM passwords
        LEFT JOIN categories ON categories.id = passwords.category_id
        ORDER BY
            CASE
                WHEN passwords.updated_at > passwords.created_at THEN passwords.updated_at
                ELSE passwords.created_at
            END DESC,
            passwords.id DESC
        LIMIT :limit
        """
    )
    fun observeRecentPasswords(limit: Int): Flow<List<PasswordEntity>>

    @Query(
        """
        SELECT
            passwords.id,
            passwords.title,
            passwords.icon_key
        FROM passwords
        WHERE passwords.title LIKE :searchPattern ESCAPE '\'
        ORDER BY passwords.title COLLATE NOCASE ASC
        """
    )
    fun observePasswordSearchResults(searchPattern: String): Flow<List<PasswordSearchResultEntity>>

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
            passwords.icon_key,
            passwords.note,
            passwords.password_fingerprint,
            passwords.created_at,
            passwords.updated_at
        FROM passwords
        LEFT JOIN categories ON categories.id = passwords.category_id
        WHERE passwords.id = :passwordId
        LIMIT 1
        """
    )
    suspend fun getPasswordById(passwordId: Long): PasswordEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(password: PasswordEntity): Long

    @androidx.room.Update
    suspend fun update(password: PasswordEntity)

    @Query("SELECT COUNT(*) FROM passwords")
    suspend fun countPasswords(): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM passwords
        WHERE password_fingerprint = :passwordFingerprint
            AND (:excludePasswordId IS NULL OR id != :excludePasswordId)
        """
    )
    suspend fun countPasswordsWithFingerprint(
        passwordFingerprint: String,
        excludePasswordId: Long?
    ): Int

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
            passwords.icon_key,
            passwords.note,
            passwords.password_fingerprint,
            passwords.created_at,
            passwords.updated_at
        FROM passwords
        LEFT JOIN categories ON categories.id = passwords.category_id
        WHERE passwords.password_fingerprint IS NULL OR passwords.password_fingerprint = ''
        """
    )
    suspend fun getPasswordsMissingFingerprint(): List<PasswordEntity>

    @Query(
        """
        UPDATE passwords
        SET password_fingerprint = :passwordFingerprint
        WHERE id = :passwordId
        """
    )
    suspend fun updatePasswordFingerprint(passwordId: Long, passwordFingerprint: String)
}
