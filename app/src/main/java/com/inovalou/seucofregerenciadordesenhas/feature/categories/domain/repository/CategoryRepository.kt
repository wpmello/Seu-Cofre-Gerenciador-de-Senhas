package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    suspend fun createCategory(
        name: String,
        iconKey: String
    ): Long

    suspend fun getCategoryById(categoryId: Long): Category?

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategoryById(categoryId: Long)

    fun observeCategories(): Flow<List<Category>>
}
