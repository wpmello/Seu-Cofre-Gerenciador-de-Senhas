package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon

import com.inovalou.seucofregerenciadordesenhas.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryIconCatalogTest {

    private val catalog = DefaultCategoryIconCatalog()

    @Test
    fun givenCatalog_whenListingIcons_thenExposesFixedTwelveOptions() {
        val icons = catalog.all()

        assertEquals(12, icons.size)
        assertEquals("ic_directory", icons.first().iconKey)
    }

    @Test
    fun givenKnownIconKey_whenResolving_thenReturnsMatchingDrawable() {
        val icon = catalog.resolve("ic_padlock")

        assertEquals("ic_padlock", icon.iconKey)
        assertEquals(R.drawable.ic_padlock, icon.drawableResId)
    }

    @Test
    fun givenUnknownIconKey_whenResolving_thenReturnsSafeFallback() {
        val fallback = catalog.default()
        val resolved = catalog.resolve("unknown_icon")

        assertEquals(fallback, resolved)
        assertTrue(resolved.iconKey.isNotBlank())
    }
}
