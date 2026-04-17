package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon

import androidx.annotation.DrawableRes
import com.inovalou.seucofregerenciadordesenhas.R
import javax.inject.Inject

data class CategoryIconOption(
    val iconKey: String,
    @DrawableRes val drawableResId: Int
)

interface CategoryIconCatalog {

    fun all(): List<CategoryIconOption>

    fun resolve(iconKey: String): CategoryIconOption

    fun default(): CategoryIconOption
}

class DefaultCategoryIconCatalog @Inject constructor() : CategoryIconCatalog {

    private val icons = listOf(
        CategoryIconOption(iconKey = "ic_directory", drawableResId = R.drawable.ic_directory),
        CategoryIconOption(iconKey = "ic_work_bag_add_category", drawableResId = R.drawable.ic_work_bag_add_category),
        CategoryIconOption(iconKey = "ic_global", drawableResId = R.drawable.ic_global),
        CategoryIconOption(iconKey = "ic_favorite", drawableResId = R.drawable.ic_favorite),
        CategoryIconOption(iconKey = "ic_home_2", drawableResId = R.drawable.ic_home_2),
        CategoryIconOption(iconKey = "ic_work_bag", drawableResId = R.drawable.ic_work_bag),
        CategoryIconOption(iconKey = "ic_home", drawableResId = R.drawable.ic_home),
        CategoryIconOption(iconKey = "ic_padlock", drawableResId = R.drawable.ic_padlock),
        CategoryIconOption(iconKey = "ic_star", drawableResId = R.drawable.ic_star),
        CategoryIconOption(iconKey = "ic_cloud", drawableResId = R.drawable.ic_cloud),
        CategoryIconOption(iconKey = "ic_user_profile", drawableResId = R.drawable.ic_user_profile),
        CategoryIconOption(iconKey = "ic_device", drawableResId = R.drawable.ic_device)
    )

    override fun all(): List<CategoryIconOption> = icons

    override fun resolve(iconKey: String): CategoryIconOption =
        icons.firstOrNull { option -> option.iconKey == iconKey } ?: default()

    override fun default(): CategoryIconOption = icons.first()
}
