package com.sumberrejeki.pos.domain.repository

import com.sumberrejeki.pos.domain.model.LicenseInfo
import kotlinx.coroutines.flow.Flow

interface LicenseRepository {
    val licenseInfoFlow: Flow<LicenseInfo>

    suspend fun getInstallationId(): String
    suspend fun verifyAndActivateLicense(licenseKeyString: String): Result<LicenseInfo>
    suspend fun deactivateLicense()
    suspend fun updateStoreName(newStoreName: String)
}
