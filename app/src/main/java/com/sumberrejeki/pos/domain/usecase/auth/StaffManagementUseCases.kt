package com.sumberrejeki.pos.domain.usecase.auth

import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.model.UserRole
import com.sumberrejeki.pos.domain.repository.AuthRepository
import com.sumberrejeki.pos.utils.security.PinHasher
import kotlinx.coroutines.flow.Flow

class GetStaffUsersUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<List<User>> = authRepository.allUsersFlow
}

class AddStaffUserUseCase(
    private val authRepository: AuthRepository,
    private val validateUserLimitUseCase: ValidateUserLimitUseCase
) {
    suspend operator fun invoke(username: String, pin: String, role: UserRole): Result<User> {
        val limitCheck = validateUserLimitUseCase()
        if (limitCheck.isFailure) {
            return Result.failure(limitCheck.exceptionOrNull() ?: IllegalStateException("Batas pengguna tercapai"))
        }

        val cleanName = username.trim()
        if (cleanName.length < 3) {
            return Result.failure(IllegalArgumentException("Nama kasir minimal 3 karakter"))
        }
        if (pin.length < 4) {
            return Result.failure(IllegalArgumentException("PIN minimal 4 digit"))
        }

        val existing = authRepository.getUserByUsername(cleanName)
        if (existing != null) {
            return Result.failure(IllegalStateException("Pengguna '$cleanName' sudah ada"))
        }

        val pinHash = PinHasher.hashPin(pin)
        val user = User(
            username = cleanName,
            pinHash = pinHash,
            role = role
        )

        val id = authRepository.registerUser(user)
        return Result.success(user.copy(userId = id))
    }
}

class DeleteStaffUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: Long, currentAdminId: Long): Result<Unit> {
        if (userId == currentAdminId) {
            return Result.failure(IllegalStateException("Tidak dapat menghapus akun Admin yang sedang aktif"))
        }
        val user = authRepository.getUserById(userId)
            ?: return Result.failure(NoSuchElementException("Pengguna tidak ditemukan"))

        authRepository.deleteUser(userId)
        return Result.success(Unit)
    }
}

class GetCurrentSessionUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> = authRepository.currentSessionFlow
}

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
