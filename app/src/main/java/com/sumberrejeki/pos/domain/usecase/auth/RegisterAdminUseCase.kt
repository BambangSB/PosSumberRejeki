package com.sumberrejeki.pos.domain.usecase.auth

import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.repository.AuthRepository
import com.sumberrejeki.pos.utils.security.PinHasher

class RegisterAdminUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, pin: String): Result<User> {
        val cleanName = username.trim()
        if (cleanName.length < 3) {
            return Result.failure(IllegalArgumentException("Nama pengguna minimal 3 karakter"))
        }
        if (pin.length < 4) {
            return Result.failure(IllegalArgumentException("PIN minimal 4 digit"))
        }

        val existing = authRepository.getUserByUsername(cleanName)
        if (existing != null) {
            return Result.failure(IllegalStateException("Pengguna '$cleanName' sudah terdaftar"))
        }

        val pinHash = PinHasher.hashPin(pin)
        val adminUser = User(
            username = cleanName,
            pinHash = pinHash,
            role = UserRole.ADMIN
        )

        val id = authRepository.registerUser(adminUser)
        val createdUser = adminUser.copy(userId = id)
        authRepository.saveSession(createdUser)
        return Result.success(createdUser)
    }
}
