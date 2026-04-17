package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.mapper

import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryEntity
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
}
