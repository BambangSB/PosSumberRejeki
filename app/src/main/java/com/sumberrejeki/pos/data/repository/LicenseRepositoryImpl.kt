package com.sumberrejeki.pos.data.repository

import android.content.Context
import com.sumberrejeki.pos.data.preferences.LicensePreferences
import com.sumberrejeki.pos.domain.model.LicenseInfo
import com.sumberrejeki.pos.domain.repository.LicenseRepository
import com.sumberrejeki.pos.utils.security.InstallationIdProvider
import com.sumberrejeki.pos.utils.security.RsaKeyHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LicenseRepositoryImpl(
    private val context: Context,
    private val licensePreferences: LicensePreferences,
    private val installationIdProvider: InstallationIdProvider
) : LicenseRepository {

    override val licenseInfoFlow: Flow<LicenseInfo> = licensePreferences.licenseInfoFlow.map { info ->
        info.copy(installationId = installationIdProvider.getInstallationId())
    }

    override suspend fun getInstallationId(): String {
        return installationIdProvider.getInstallationId()
    }

    override suspend fun verifyAndActivateLicense(licenseKeyString: String): Result<LicenseInfo> {
        val installationId = getInstallationId()
        val publicKey = RsaKeyHelper.loadPublicKeyFromRawResource(context)

        val verifyResult = RsaKeyHelper.verifyLicense(
            licenseString = licenseKeyString,
            currentInstallationId = installationId,
            publicKey = publicKey
        )

        return verifyResult.mapCatching { payload ->
            val isPremium = payload.tier.equals("PREMIUM", ignoreCase = true) ||
                    payload.tier.equals("PRO", ignoreCase = true)
            val activationDate = System.currentTimeMillis()

            licensePreferences.saveLicense(
                isPremium = isPremium,
                licenseKey = licenseKeyString.trim(),
                licensedTo = payload.licensedTo,
                activationDate = activationDate,
                expiresAt = payload.expiresAt
            )

            LicenseInfo(
                isPremium = isPremium,
                installationId = installationId,
                licensedTo = payload.licensedTo,
                licenseKey = licenseKeyString.trim(),
                activationDate = activationDate,
                expiresAt = payload.expiresAt
            )
        }
    }

    override suspend fun deactivateLicense() {
        licensePreferences.clearLicense()
    }

    override suspend fun updateStoreName(newStoreName: String) {
        licensePreferences.updateStoreName(newStoreName)
    }
}
