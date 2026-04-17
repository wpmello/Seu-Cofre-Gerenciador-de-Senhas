package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.mapper

import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryEntity
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryMapperTest {

    @Test
    fun givenCategoryEntity_whenMappingToDomain_thenCopiesAllFields() {
        val entity = CategoryEntity(
            id = 7L,
            name = "Trabalho",
            iconKey = "ic_work_bag_add_category",
            itemCount = 42
        )

        val category = entity.toDomain()

        assertEquals(7L, category.id)
        assertEquals("Trabalho", category.name)
        assertEquals("ic_work_bag_add_category", category.iconKey)
        assertEquals(42, category.itemCount)
    }

    @Test
    fun givenDomainCategory_whenMappingToEntity_thenCopiesAllFields() {
        val category = Category(
            id = 8L,
            name = "Pessoal",
            iconKey = "ic_user_profile",
            itemCount = 3
        )

        val entity = category.toEntity()

        assertEquals(8L, entity.id)
        assertEquals("Pessoal", entity.name)
        assertEquals("ic_user_profile", entity.iconKey)
        assertEquals(3, entity.itemCount)
    }
}
