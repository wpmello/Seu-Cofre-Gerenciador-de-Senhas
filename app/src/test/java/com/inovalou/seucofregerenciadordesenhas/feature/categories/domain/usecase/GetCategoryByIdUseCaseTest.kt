package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCategoryByIdUseCaseTest {

    @Test
    fun givenPersistedCategoryId_whenGettingCategory_thenReturnsRepositoryResult() = runTest {
        val repository = FakeCategoryRepository(
            category = Category(
                id = 14L,
                name = "Financeiro",
                iconKey = "ic_star",
                itemCount = 10,
                lastModifiedAt = 0L
            )
        )
        val useCase = GetCategoryByIdUseCase(repository)

        val result = useCase(14L)

        assertEquals(14L, result?.id)
        assertEquals("Financeiro", result?.name)
        assertEquals("ic_star", result?.iconKey)
        assertEquals(10, result?.itemCount)
    }

    @Test
    fun givenUnknownCategoryId_whenGettingCategory_thenReturnsNull() = runTest {
        val useCase = GetCategoryByIdUseCase(FakeCategoryRepository(category = null))

        val result = useCase(999L)

        assertEquals(null, result)
    }

    private class FakeCategoryRepository(
        private val category: Category?
    ) : CategoryRepository {

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = category

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()
    }
}
