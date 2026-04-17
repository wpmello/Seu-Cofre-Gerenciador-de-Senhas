package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CategoriesLocalDataSource {

    suspend fun insertCategory(category: CategoryEntity): Long

    fun observeCategories(): Flow<List<CategoryEntity>>
}

class RoomCategoriesLocalDataSource @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoriesLocalDataSource {

    override suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    override fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeCategories()
}
