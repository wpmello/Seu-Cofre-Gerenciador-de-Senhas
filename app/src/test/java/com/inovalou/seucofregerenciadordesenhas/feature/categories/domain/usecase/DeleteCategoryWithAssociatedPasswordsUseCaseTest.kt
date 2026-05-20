package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteCategoryWithAssociatedPasswordsUseCaseTest {

    @Test
    fun givenExistingCategory_whenDeletingWithAssociatedPasswords_thenDelegatesTransactionalDeleteToRepository() = runTest {
        val repository = FakeCategoryRepository(existingCategory = category(id = 18L))
        val useCase = DeleteCategoryWithAssociatedPasswordsUseCase(repository)

        val result = useCase(18L)

        assertTrue(result is DeleteCategoryResult.Success)
        assertEquals(18L, repository.deletedCategoryWithAssociatedPasswordsId)
        assertEquals(null, repository.deletedCategoryId)
    }

    @Test
    fun givenUnknownCategoryId_whenDeletingWithAssociatedPasswords_thenReturnsNotFound() = runTest {
        val useCase = DeleteCategoryWithAssociatedPasswordsUseCase(
            FakeCategoryRepository(existingCategory = null)
        )

        val result = useCase(71L)

        assertTrue(result is DeleteCategoryResult.NotFound)
    }

    @Test
    fun givenRepositoryFailure_whenDeletingWithAssociatedPasswords_thenReturnsFailure() = runTest {
        val useCase = DeleteCategoryWithAssociatedPasswordsUseCase(
            FakeCategoryRepository(
                existingCategory = category(id = 22L),
                shouldFailOnDeleteWithAssociatedPasswords = true
            )
        )

        val result = useCase(22L)

        assertTrue(result is DeleteCategoryResult.Failure)
    }

    private class FakeCategoryRepository(
        private val existingCategory: Category?,
        private val shouldFailOnDeleteWithAssociatedPasswords: Boolean = false
    ) : CategoryRepository {
        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long
        ) = Unit


        var deletedCategoryId: Long? = null
        var deletedCategoryWithAssociatedPasswordsId: Long? = null

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = existingCategory

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) {
            deletedCategoryId = categoryId
        }

        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) {
            if (shouldFailOnDeleteWithAssociatedPasswords) error("delete with associated failure")
            deletedCategoryWithAssociatedPasswordsId = categoryId
        }

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()
    }
}

private fun category(
    id: Long
) = Category(
    id = id,
    name = "Pessoal",
    iconKey = "ic_directory",
    itemCount = 0,
    lastModifiedAt = 0L
)
