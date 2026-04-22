package com.inovalou.seucofregerenciadordesenhas.core.ui.icon

import androidx.annotation.DrawableRes
import com.inovalou.seucofregerenciadordesenhas.R
import javax.inject.Inject

data class VaultIconOption(
    val iconKey: String,
    @DrawableRes val drawableResId: Int
)

interface VaultIconCatalog {

    fun all(): List<VaultIconOption>

    fun resolve(iconKey: String): VaultIconOption

    fun default(): VaultIconOption
}

class DefaultVaultIconCatalog @Inject constructor() : VaultIconCatalog {

    private val icons = listOf(
        VaultIconOption(iconKey = "ic_directory", drawableResId = R.drawable.ic_directory),
        VaultIconOption(iconKey = "ic_work_bag_add_category", drawableResId = R.drawable.ic_work_bag_add_category),
        VaultIconOption(iconKey = "ic_global", drawableResId = R.drawable.ic_global),
        VaultIconOption(iconKey = "ic_favorite", drawableResId = R.drawable.ic_favorite),
        VaultIconOption(iconKey = "ic_home_2", drawableResId = R.drawable.ic_home_2),
        VaultIconOption(iconKey = "ic_work_bag", drawableResId = R.drawable.ic_work_bag),
        VaultIconOption(iconKey = "ic_home", drawableResId = R.drawable.ic_home),
        VaultIconOption(iconKey = "ic_padlock", drawableResId = R.drawable.ic_padlock),
        VaultIconOption(iconKey = "ic_star", drawableResId = R.drawable.ic_star),
        VaultIconOption(iconKey = "ic_cloud", drawableResId = R.drawable.ic_cloud),
        VaultIconOption(iconKey = "ic_user_profile", drawableResId = R.drawable.ic_user_profile),
        VaultIconOption(iconKey = "ic_device", drawableResId = R.drawable.ic_device)
    )

    override fun all(): List<VaultIconOption> = icons

    override fun resolve(iconKey: String): VaultIconOption =
        icons.firstOrNull { option -> option.iconKey == iconKey } ?: default()

    override fun default(): VaultIconOption = icons.first()
}
