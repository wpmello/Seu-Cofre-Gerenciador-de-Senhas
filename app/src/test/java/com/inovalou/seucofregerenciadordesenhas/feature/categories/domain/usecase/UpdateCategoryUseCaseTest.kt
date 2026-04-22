package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCategoryUseCaseTest {

    @Test
    fun givenValidInput_whenUpdatingCategory_thenPersistsTrimmedNameAndIconKey() = runTest {
        val repository = FakeCategoryRepository(
            existingCategory = Category(
                id = 3L,
                name = "Trabalho",
                iconKey = "ic_work_bag_add_category",
                itemCount = 7,
                lastModifiedAt = 10L
            )
        )
        val useCase = UpdateCategoryUseCase(repository)

        val result = useCase(
            categoryId = 3L,
            name = "  Corporativo  ",
            iconKey = "ic_directory"
        )

        assertTrue(result is UpdateCategoryResult.Success)
        assertEquals(
            Category(
                id = 3L,
                name = "Corporativo",
                iconKey = "ic_directory",
                itemCount = 7,
                lastModifiedAt = 10L
            ),
            repository.updatedCategory
        )
    }

    @Test
    fun givenBlankName_whenUpdatingCategory_thenReturnsValidationError() = runTest {
        val useCase = UpdateCategoryUseCase(
            FakeCategoryRepository(
                existingCategory = Category(
                    id = 2L,
                    name = "Pessoal",
                    iconKey = "ic_directory",
                    itemCount = 0,
                    lastModifiedAt = 0L
                )
            )
        )

        val result = useCase(categoryId = 2L, name = "   ", iconKey = "ic_directory")

        assertTrue(result is UpdateCategoryResult.ValidationError)
        result as UpdateCategoryResult.ValidationError
        assertEquals(UpdateCategoryNameError.Blank, result.validation.nameError)
    }

    @Test
    fun givenMissingIcon_whenUpdatingCategory_thenReturnsValidationError() = runTest {
        val useCase = UpdateCategoryUseCase(
            FakeCategoryRepository(
                existingCategory = Category(
                    id = 2L,
                    name = "Pessoal",
                    iconKey = "ic_directory",
                    itemCount = 0,
                    lastModifiedAt = 0L
                )
            )
        )

        val result = useCase(categoryId = 2L, name = "Pessoal", iconKey = null)

        assertTrue(result is UpdateCategoryResult.ValidationError)
        result as UpdateCategoryResult.ValidationError
        assertEquals(UpdateCategoryIconError.Missing, result.validation.iconError)
    }

    @Test
    fun givenUnknownCategoryId_whenUpdatingCategory_thenReturnsNotFound() = runTest {
        val useCase = UpdateCategoryUseCase(FakeCategoryRepository(existingCategory = null))

        val result = useCase(categoryId = 55L, name = "Pessoal", iconKey = "ic_directory")

        assertTrue(result is UpdateCategoryResult.NotFound)
    }

    @Test
    fun givenRepositoryFailure_whenUpdatingCategory_thenReturnsFailure() = runTest {
        val useCase = UpdateCategoryUseCase(
            FakeCategoryRepository(
                existingCategory = Category(
                    id = 8L,
                    name = "Saúde",
                    iconKey = "ic_padlock",
                    itemCount = 2,
                    lastModifiedAt = 0L
                ),
                shouldFailOnUpdate = true
            )
        )

        val result = useCase(categoryId = 8L, name = "Saúde", iconKey = "ic_star")

        assertTrue(result is UpdateCategoryResult.Failure)
    }

    private class FakeCategoryRepository(
        private val existingCategory: Category?,
        private val shouldFailOnUpdate: Boolean = false
    ) : CategoryRepository {

        var updatedCategory: Category? = null

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = existingCategory

        override suspend fun updateCategory(category: Category) {
            if (shouldFailOnUpdate) error("update failure")
            updatedCategory = category
        }

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()
    }
}
