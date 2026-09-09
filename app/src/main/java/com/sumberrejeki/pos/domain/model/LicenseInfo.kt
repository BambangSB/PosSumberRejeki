package com.sumberrejeki.pos.domain.model

enum class SubscriptionTier(val displayName: String) {
    FREE("Gratis"),
    PRO_TRIAL("Pro (Trial)"),
    PRO("Pro"),
    BUSINESS("Bisnis (Coming Soon)")
}

data class LicenseInfo(
    val isPremium: Boolean = false,
    val isTrialActive: Boolean = false,
    val trialDaysRemaining: Int = 0,
    val trialHoursRemaining: Int = 0,
    val firstInstallTime: Long? = null,
    val expiresAt: Long? = null,
    val installationId: String = "",
    val licensedTo: String = "Sumber Rejeki POS",
    val licenseKey: String = "",
    val activationDate: Long? = null
) {
    val isProActive: Boolean
        get() = isPremium || isTrialActive

    val maxUsers: Int
        get() = if (isProActive) 3 else 1

    val tier: SubscriptionTier
        get() = when {
            isPremium -> SubscriptionTier.PRO
            isTrialActive -> SubscriptionTier.PRO_TRIAL
            else -> SubscriptionTier.FREE
        }

    // 6 Free Tier Limitation Helpers
    val maxProductsAllowed: Int
        get() = if (isProActive) Int.MAX_VALUE else 30

    fun canAddProduct(currentProductCount: Int): Boolean {
        return isProActive || currentProductCount < 30
    }

    val canEditProductDetails: Boolean
        get() = isProActive

    val canViewProfitDetails: Boolean
        get() = isProActive

    val canExportData: Boolean
        get() = isProActive

    val canEditStoreName: Boolean
        get() = isProActive

    val canAddCashier: Boolean
        get() = isProActive
}
