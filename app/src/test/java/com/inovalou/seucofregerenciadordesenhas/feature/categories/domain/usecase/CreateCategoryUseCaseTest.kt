package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateCategoryUseCaseTest {

    @Test
    fun givenValidInput_whenCreatingCategory_thenPersistsTrimmedNameAndIconKey() = runTest {
        val repository = FakeCategoryRepository()
        val useCase = CreateCategoryUseCase(repository)

        val result = useCase(
            name = "  Trabalho  ",
            iconKey = "ic_work_bag_add_category"
        )

        assertTrue(result is CreateCategoryResult.Success)
        assertEquals("Trabalho", repository.createdName)
        assertEquals("ic_work_bag_add_category", repository.createdIconKey)
    }

    @Test
    fun givenBlankName_whenCreatingCategory_thenReturnsValidationError() = runTest {
        val repository = FakeCategoryRepository()
        val useCase = CreateCategoryUseCase(repository)

        val result = useCase(
            name = "   ",
            iconKey = "ic_directory"
        )

        assertTrue(result is CreateCategoryResult.ValidationError)
        result as CreateCategoryResult.ValidationError
        assertEquals(CreateCategoryNameError.Blank, result.validation.nameError)
    }

    @Test
    fun givenMissingIcon_whenCreatingCategory_thenReturnsValidationError() = runTest {
        val repository = FakeCategoryRepository()
        val useCase = CreateCategoryUseCase(repository)

        val result = useCase(
            name = "Pessoal",
            iconKey = null
        )

        assertTrue(result is CreateCategoryResult.ValidationError)
        result as CreateCategoryResult.ValidationError
        assertEquals(CreateCategoryIconError.Missing, result.validation.iconError)
    }

    @Test
    fun givenRepositoryFailure_whenCreatingCategory_thenReturnsFailure() = runTest {
        val repository = FakeCategoryRepository(shouldFailOnCreate = true)
        val useCase = CreateCategoryUseCase(repository)

        val result = useCase(
            name = "Saúde",
            iconKey = "ic_padlock"
        )

        assertTrue(result is CreateCategoryResult.Failure)
    }

    private class FakeCategoryRepository(
        private val shouldFailOnCreate: Boolean = false
    ) : CategoryRepository {

        var createdName: String? = null
        var createdIconKey: String? = null

        override suspend fun createCategory(name: String, iconKey: String): Long {
            if (shouldFailOnCreate) error("create failure")
            createdName = name
            createdIconKey = iconKey
            return 1L
        }

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()
    }
}
