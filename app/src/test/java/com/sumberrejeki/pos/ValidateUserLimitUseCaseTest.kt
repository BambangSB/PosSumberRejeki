package com.sumberrejeki.pos

import com.sumberrejeki.pos.domain.model.LicenseInfo
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.repository.AuthRepository
import com.sumberrejeki.pos.domain.repository.LicenseRepository
import com.sumberrejeki.pos.domain.usecase.auth.ValidateUserLimitUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateUserLimitUseCaseTest {

    private class FakeAuthRepository(private var count: Int) : AuthRepository {
        override val currentSessionFlow: Flow<User?> = flowOf(null)
        override val allUsersFlow: Flow<List<User>> = flowOf(emptyList())
        override val userCountFlow: Flow<Int> = flowOf(count)

        override suspend fun getUserCount(): Int = count
        override suspend fun getUserByUsername(username: String): User? = null
        override suspend fun getUserById(userId: Long): User? = null
        override suspend fun registerUser(user: User): Long = 1L
        override suspend fun deleteUser(userId: Long) {}
        override suspend fun saveSession(user: User) {}
        override suspend fun logout() {}
    }

    private class FakeLicenseRepository(private val isPremium: Boolean) : LicenseRepository {
        override val licenseInfoFlow: Flow<LicenseInfo> = flowOf(
            LicenseInfo(
                isPremium = isPremium,
                maxUsers = if (isPremium) 3 else 1
            )
        )
        override suspend fun getInstallationId(): String = "FAKE-ID"
        override suspend fun verifyAndActivateLicense(licenseKeyString: String): Result<LicenseInfo> =
            Result.success(LicenseInfo(isPremium = true, maxUsers = 3))
        override suspend fun deactivateLicense() {}
    }

    @Test
    fun `free tier with 0 users allows adding 1 user`() = runTest {
        val authRepo = FakeAuthRepository(0)
        val licenseRepo = FakeLicenseRepository(isPremium = false)
        val useCase = ValidateUserLimitUseCase(authRepo, licenseRepo)

        val result = useCase()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `free tier with 1 user rejects adding second user`() = runTest {
        val authRepo = FakeAuthRepository(1)
        val licenseRepo = FakeLicenseRepository(isPremium = false)
        val useCase = ValidateUserLimitUseCase(authRepo, licenseRepo)

        val result = useCase()
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("Gratis") == true)
    }

    @Test
    fun `premium tier with 2 users allows adding 3rd user`() = runTest {
        val authRepo = FakeAuthRepository(2)
        val licenseRepo = FakeLicenseRepository(isPremium = true)
        val useCase = ValidateUserLimitUseCase(authRepo, licenseRepo)

        val result = useCase()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `premium tier with 3 users rejects adding 4th user`() = runTest {
        val authRepo = FakeAuthRepository(3)
        val licenseRepo = FakeLicenseRepository(isPremium = true)
        val useCase = ValidateUserLimitUseCase(authRepo, licenseRepo)

        val result = useCase()
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("3 pengguna") == true)
    }
}
