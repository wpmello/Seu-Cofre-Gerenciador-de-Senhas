package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query(
        """
        SELECT
            categories.id,
            categories.name,
            categories.icon_key,
            COUNT(passwords.id) AS item_count
        FROM categories
        LEFT JOIN passwords ON passwords.category_id = categories.id
        WHERE categories.id = :categoryId
        GROUP BY categories.id
        LIMIT 1
        """
    )
    suspend fun getCategoryById(categoryId: Long): CategoryEntity?

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    @Query(
        """
        SELECT
            categories.id,
            categories.name,
            categories.icon_key,
            COUNT(passwords.id) AS item_count
        FROM categories
        LEFT JOIN passwords ON passwords.category_id = categories.id
        GROUP BY categories.id
        ORDER BY categories.name COLLATE NOCASE ASC
        """
    )
    fun observeCategories(): Flow<List<CategoryEntity>>
}
