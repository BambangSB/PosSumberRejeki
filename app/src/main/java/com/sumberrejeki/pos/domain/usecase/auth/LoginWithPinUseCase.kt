package com.sumberrejeki.pos.domain.usecase.auth

import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.repository.AuthRepository
import com.sumberrejeki.pos.utils.security.PinHasher

class LoginWithPinUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, pin: String): Result<User> {
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Pilih atau masukkan pengguna"))
        }
        if (pin.length < 4) {
            return Result.failure(IllegalArgumentException("PIN minimal 4 digit"))
        }

        val user = authRepository.getUserByUsername(username)
            ?: return Result.failure(NoSuchElementException("Pengguna '$username' tidak ditemukan"))

        val isPinValid = PinHasher.verifyPin(pin, user.pinHash)
        if (!isPinValid) {
            return Result.failure(SecurityException("PIN yang dimasukkan salah"))
        }

        authRepository.saveSession(user)
        return Result.success(user)
    }
}
