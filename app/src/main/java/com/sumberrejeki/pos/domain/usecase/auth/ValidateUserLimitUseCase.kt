package com.sumberrejeki.pos.domain.usecase.auth

import com.sumberrejeki.pos.domain.repository.AuthRepository
import com.sumberrejeki.pos.domain.repository.LicenseRepository
import kotlinx.coroutines.flow.first

class ValidateUserLimitUseCase(
    private val authRepository: AuthRepository,
    private val licenseRepository: LicenseRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        val licenseInfo = licenseRepository.licenseInfoFlow.first()
        val currentCount = authRepository.getUserCount()

        val maxAllowed = if (licenseInfo.isPremium) 3 else 1

        return if (currentCount < maxAllowed) {
            Result.success(true)
        } else {
            val message = if (licenseInfo.isPremium) {
                "Batas maksimum pengguna Premium (3 pengguna) telah tercapai."
            } else {
                "Versi Gratis hanya mengizinkan 1 pengguna (Admin). Silakan upgrade ke Premium untuk menambah kasir."
            }
            Result.failure(IllegalStateException(message))
        }
    }
}
