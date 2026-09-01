package com.sumberrejeki.pos.data.repository

import com.sumberrejeki.pos.data.local.dao.UserDao
import com.sumberrejeki.pos.data.local.entity.UserEntity
import com.sumberrejeki.pos.data.preferences.UserSessionDataStore
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val sessionDataStore: UserSessionDataStore
) : AuthRepository {

    override val currentSessionFlow: Flow<User?> = sessionDataStore.currentSessionFlow

    override val allUsersFlow: Flow<List<User>> = userDao.getAllUsersFlow().map { list ->
        list.map { it.toDomain() }
    }

    override val userCountFlow: Flow<Int> = userDao.getUserCountFlow()

    override suspend fun getUserCount(): Int = userDao.getUserCount()

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)?.toDomain()
    }

    override suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)?.toDomain()
    }

    override suspend fun registerUser(user: User): Long {
        return userDao.insertUser(UserEntity.fromDomain(user))
    }

    override suspend fun deleteUser(userId: Long) {
        userDao.deleteUserById(userId)
    }

    override suspend fun saveSession(user: User) {
        sessionDataStore.saveSession(user)
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
    }
}
