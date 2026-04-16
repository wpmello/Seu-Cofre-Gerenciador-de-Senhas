package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CategoriesLocalDataSource {

    fun observeCategories(): Flow<List<CategoryEntity>>
}

class RoomCategoriesLocalDataSource @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoriesLocalDataSource {

    override fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeCategories()
}
