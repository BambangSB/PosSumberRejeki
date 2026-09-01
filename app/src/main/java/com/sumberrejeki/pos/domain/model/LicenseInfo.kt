package com.sumberrejeki.pos.domain.model

data class LicenseInfo(
    val isPremium: Boolean = false,
    val installationId: String = "",
    val licensedTo: String = "Sumber Rejeki POS",
    val licenseKey: String = "",
    val activationDate: Long? = null,
    val maxUsers: Int = if (isPremium) 3 else 1
)
