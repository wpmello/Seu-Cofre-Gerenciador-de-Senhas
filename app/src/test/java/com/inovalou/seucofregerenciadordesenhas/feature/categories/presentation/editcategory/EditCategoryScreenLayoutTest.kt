package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import org.junit.Assert.assertEquals
import org.junit.Test

class EditCategoryScreenLayoutTest {

    @Test
    fun givenMoreThanSevenAssociatedPasswords_whenResolvingVisibleItems_thenCapsListAtSevenItems() {
        assertEquals(7, categoryPasswordsVisibleItemCount(passwordCount = 12))
    }

    @Test
    fun givenFewerThanSevenAssociatedPasswords_whenResolvingVisibleItems_thenUsesActualItemCount() {
        assertEquals(4, categoryPasswordsVisibleItemCount(passwordCount = 4))
    }
}
