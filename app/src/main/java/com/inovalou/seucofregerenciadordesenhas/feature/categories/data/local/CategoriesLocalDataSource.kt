package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local

import com.inovalou.seucofregerenciadordesenhas.core.database.DatabaseTransactionRunner
import com.inovalou.seucofregerenciadordesenhas.core.database.toSqlLikeContainsPattern
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface CategoriesLocalDataSource {

    suspend fun insertCategory(category: CategoryEntity): Long

    suspend fun getCategoryById(categoryId: Long): CategoryEntity?

    suspend fun updateCategory(category: CategoryEntity)

    suspend fun updateCategoryLastModifiedAt(categoryId: Long, lastModifiedAt: Long)

    suspend fun deleteCategoryById(categoryId: Long)

    fun observeCategories(): Flow<List<CategoryEntity>>

    fun observeCategoriesMatchingQuery(query: String): Flow<List<CategoryEntity>> =
        observeCategories().map { categories ->
            categories.filter { category ->
                category.name.contains(query.trim(), ignoreCase = true)
            }
        }

    suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long)

    suspend fun transferPasswordsToCategory(
        sourceCategoryId: Long,
        targetCategoryId: Long,
        lastModifiedAt: Long
    )
}

class RoomCategoriesLocalDataSource @Inject constructor(
    private val categoryDao: CategoryDao,
    private val passwordsDao: PasswordDao,
    private val transactionRunner: DatabaseTransactionRunner
) : CategoriesLocalDataSource {

    override suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    override suspend fun getCategoryById(categoryId: Long): CategoryEntity? =
        categoryDao.getCategoryById(categoryId)

    override suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    override suspend fun updateCategoryLastModifiedAt(categoryId: Long, lastModifiedAt: Long) {
        categoryDao.updateCategoryLastModifiedAt(categoryId, lastModifiedAt)
    }

    override suspend fun deleteCategoryById(categoryId: Long) {
        categoryDao.deleteCategoryById(categoryId)
    }

    override fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeCategories()

    override fun observeCategoriesMatchingQuery(query: String): Flow<List<CategoryEntity>> =
        categoryDao.observeCategoriesMatchingQuery(query.toSqlLikeContainsPattern())

    override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) {
        transactionRunner.runInTransaction {
            passwordsDao.deletePasswordsByCategoryId(categoryId)
            categoryDao.deleteCategoryById(categoryId)
        }
    }

    override suspend fun transferPasswordsToCategory(
        sourceCategoryId: Long,
        targetCategoryId: Long,
        lastModifiedAt: Long
    ) {
        transactionRunner.runInTransaction {
            passwordsDao.updatePasswordsCategory(
                sourceCategoryId = sourceCategoryId,
                targetCategoryId = targetCategoryId
            )
            categoryDao.updateCategoryLastModifiedAt(
                categoryId = sourceCategoryId,
                lastModifiedAt = lastModifiedAt
            )
            categoryDao.updateCategoryLastModifiedAt(
                categoryId = targetCategoryId,
                lastModifiedAt = lastModifiedAt
            )
        }
    }
}
