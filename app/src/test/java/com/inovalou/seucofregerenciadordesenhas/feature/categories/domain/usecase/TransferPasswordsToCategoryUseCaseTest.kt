package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferPasswordsToCategoryUseCaseTest {

    @Test
    fun givenValidSourceAndTarget_whenTransferring_thenDelegatesBatchTransferToRepository() = runTest {
        val repository = FakeCategoryRepository(
            categories = listOf(category(id = 1L), category(id = 2L))
        )
        val useCase = TransferPasswordsToCategoryUseCase(repository)

        val result = useCase(sourceCategoryId = 1L, targetCategoryId = 2L)

        assertTrue(result is TransferPasswordsToCategoryResult.Success)
        assertEquals(1L to 2L, repository.transfers.single())
    }

    @Test
    fun givenSameSourceAndTarget_whenTransferring_thenReturnsSameCategoryWithoutRepositoryMutation() = runTest {
        val repository = FakeCategoryRepository(
            categories = listOf(category(id = 1L))
        )
        val useCase = TransferPasswordsToCategoryUseCase(repository)

        val result = useCase(sourceCategoryId = 1L, targetCategoryId = 1L)

        assertTrue(result is TransferPasswordsToCategoryResult.SameCategory)
        assertTrue(repository.transfers.isEmpty())
    }

    @Test
    fun givenMissingSource_whenTransferring_thenReturnsSourceNotFound() = runTest {
        val useCase = TransferPasswordsToCategoryUseCase(
            FakeCategoryRepository(categories = listOf(category(id = 2L)))
        )

        val result = useCase(sourceCategoryId = 1L, targetCategoryId = 2L)

        assertTrue(result is TransferPasswordsToCategoryResult.SourceNotFound)
    }

    @Test
    fun givenMissingTarget_whenTransferring_thenReturnsTargetNotFound() = runTest {
        val useCase = TransferPasswordsToCategoryUseCase(
            FakeCategoryRepository(categories = listOf(category(id = 1L)))
        )

        val result = useCase(sourceCategoryId = 1L, targetCategoryId = 2L)

        assertTrue(result is TransferPasswordsToCategoryResult.TargetNotFound)
    }

    @Test
    fun givenRepositoryFailure_whenTransferring_thenReturnsFailure() = runTest {
        val useCase = TransferPasswordsToCategoryUseCase(
            FakeCategoryRepository(
                categories = listOf(category(id = 1L), category(id = 2L)),
                shouldFailOnTransfer = true
            )
        )

        val result = useCase(sourceCategoryId = 1L, targetCategoryId = 2L)

        assertTrue(result is TransferPasswordsToCategoryResult.Failure)
    }

    private class FakeCategoryRepository(
        categories: List<Category>,
        private val shouldFailOnTransfer: Boolean = false
    ) : CategoryRepository {
        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) = Unit


        private val categoryById = categories.associateBy { it.id }
        val transfers = mutableListOf<Pair<Long, Long>>()

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = categoryById[categoryId]

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long
        ) {
            if (shouldFailOnTransfer) error("transfer failure")
            transfers += sourceCategoryId to targetCategoryId
        }

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()
    }
}

private fun category(
    id: Long
) = Category(
    id = id,
    name = "Categoria $id",
    iconKey = "ic_directory",
    itemCount = 0,
    lastModifiedAt = 0L
)
