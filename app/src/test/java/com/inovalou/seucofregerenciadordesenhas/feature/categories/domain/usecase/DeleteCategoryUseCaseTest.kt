package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteCategoryUseCaseTest {

    @Test
    fun givenExistingCategory_whenDeleting_thenDelegatesDeleteToRepository() = runTest {
        val repository = FakeCategoryRepository(
            existingCategory = Category(
                id = 18L,
                name = "Pessoal",
                iconKey = "ic_directory",
                itemCount = 0,
                lastModifiedAt = 0L
            )
        )
        val useCase = DeleteCategoryUseCase(repository)

        val result = useCase(18L)

        assertTrue(result is DeleteCategoryResult.Success)
        assertEquals(18L, repository.deletedCategoryId)
    }

    @Test
    fun givenUnknownCategoryId_whenDeleting_thenReturnsNotFound() = runTest {
        val useCase = DeleteCategoryUseCase(FakeCategoryRepository(existingCategory = null))

        val result = useCase(71L)

        assertTrue(result is DeleteCategoryResult.NotFound)
    }

    @Test
    fun givenRepositoryFailure_whenDeleting_thenReturnsFailure() = runTest {
        val useCase = DeleteCategoryUseCase(
            FakeCategoryRepository(
                existingCategory = Category(
                    id = 22L,
                    name = "Work",
                    iconKey = "ic_work_bag",
                    itemCount = 5,
                    lastModifiedAt = 0L
                ),
                shouldFailOnDelete = true
            )
        )

        val result = useCase(22L)

        assertTrue(result is DeleteCategoryResult.Failure)
    }

    private class FakeCategoryRepository(
        private val existingCategory: Category?,
        private val shouldFailOnDelete: Boolean = false
    ) : CategoryRepository {

        var deletedCategoryId: Long? = null

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = existingCategory

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) {
            if (shouldFailOnDelete) error("delete failure")
            deletedCategoryId = categoryId
        }

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()
    }
}
