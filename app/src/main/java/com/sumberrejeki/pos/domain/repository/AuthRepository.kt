package com.sumberrejeki.pos.domain.repository

import com.sumberrejeki.pos.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentSessionFlow: Flow<User?>
    val allUsersFlow: Flow<List<User>>
    val userCountFlow: Flow<Int>

    suspend fun getUserCount(): Int
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserById(userId: Long): User?
    suspend fun registerUser(user: User): Long
    suspend fun deleteUser(userId: Long)
    suspend fun saveSession(user: User)
    suspend fun logout()
}
