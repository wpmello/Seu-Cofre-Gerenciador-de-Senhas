package com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHome
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHomeCategory
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHomePassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveRecentPasswordsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecurityDetailsUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

class ObserveVaultHomeUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val passwordRepository: PasswordRepository,
    private val observeRecentPasswordsUseCase: ObserveRecentPasswordsUseCase,
    private val observeVaultSecurityDetailsUseCase: ObserveVaultSecurityDetailsUseCase
) {

    operator fun invoke() = combine(
        categoryRepository.observeCategories(),
        passwordRepository.observePasswordCount(),
        observeRecentPasswordsUseCase(RecentPasswordLimit),
        observeVaultSecurityDetailsUseCase()
    ) { categories, totalPasswords, recentPasswords, securityDetails ->
        VaultHome(
            totalPasswords = totalPasswords,
            weakPasswords = securityDetails.countWeakPasswords(),
            moderatePasswords = securityDetails.countModeratePasswords(),
            strongPasswords = securityDetails.countStrongPasswords(),
            categories = categories.toHomeCategories(),
            showOtherCategories = categories.size >= HomeCategoryLimit,
            recentPasswords = recentPasswords.toRecentHomePasswords()
        )
    }

    private fun List<Category>.toHomeCategories(): List<VaultHomeCategory> =
        sortedWith(
            compareByDescending<Category> { category -> category.lastModifiedAt }
                .thenByDescending { category -> category.id }
        )
            .take(HomeCategoryLimit)
            .map { category ->
                VaultHomeCategory(
                    id = category.id,
                    name = category.name,
                    iconKey = category.iconKey,
                    itemCount = category.itemCount,
                    lastModifiedAt = category.lastModifiedAt
                )
            }

    private fun List<PasswordSummary>.toRecentHomePasswords(): List<VaultHomePassword> =
        sortedWith(
            compareByDescending<PasswordSummary> { password ->
                maxOf(password.createdAt, password.updatedAt)
            }.thenByDescending { password -> password.id }
        )
            .take(RecentPasswordLimit)
            .map { password ->
                VaultHomePassword(
                    id = password.id,
                    title = password.title,
                    login = password.login,
                    securityRiskLevel = password.securityRiskLevel,
                    createdAt = password.createdAt,
                    updatedAt = password.updatedAt
                )
            }

    private fun VaultSecurityDetails.countWeakPasswords(): Int =
        countFor(PasswordSecurityBucket.Weak)

    private fun VaultSecurityDetails.countModeratePasswords(): Int =
        countFor(PasswordSecurityBucket.Moderate)

    private fun VaultSecurityDetails.countStrongPasswords(): Int =
        countFor(PasswordSecurityBucket.Safe)

    private companion object {
        const val HomeCategoryLimit = 3
        const val RecentPasswordLimit = 4
    }
}
