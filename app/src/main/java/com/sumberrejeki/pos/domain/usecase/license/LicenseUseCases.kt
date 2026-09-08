package com.sumberrejeki.pos.domain.usecase.license

import com.sumberrejeki.pos.domain.model.LicenseInfo
import com.sumberrejeki.pos.domain.repository.LicenseRepository
import kotlinx.coroutines.flow.Flow

class VerifyRsaLicenseUseCase(
    private val licenseRepository: LicenseRepository
) {
    suspend operator fun invoke(licenseKey: String): Result<LicenseInfo> {
        if (licenseKey.isBlank()) {
            return Result.failure(IllegalArgumentException("Kode lisensi tidak boleh kosong"))
        }
        return licenseRepository.verifyAndActivateLicense(licenseKey.trim())
    }
}

class GetLicenseStatusUseCase(
    private val licenseRepository: LicenseRepository
) {
    val licenseInfoFlow: Flow<LicenseInfo> = licenseRepository.licenseInfoFlow

    suspend fun getInstallationId(): String = licenseRepository.getInstallationId()

    suspend fun deactivateLicense() = licenseRepository.deactivateLicense()
}
