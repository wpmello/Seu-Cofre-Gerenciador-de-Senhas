package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface CategoryRepository {

    suspend fun createCategory(
        name: String,
        iconKey: String
    ): Long

    suspend fun getCategoryById(categoryId: Long): Category?

    suspend fun updateCategory(category: Category)

    suspend fun touchCategory(categoryId: Long)

    suspend fun deleteCategoryById(categoryId: Long)

    fun observeCategories(): Flow<List<Category>>

    fun observeCategoriesMatchingQuery(query: String): Flow<List<Category>> =
        observeCategories().map { categories ->
            categories.filter { category ->
                category.name.contains(query.trim(), ignoreCase = true)
            }
        }
}
