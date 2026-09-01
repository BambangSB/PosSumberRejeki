package com.sumberrejeki.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val username: String,
    val pinHash: String,
    val role: String, // "ADMIN" | "CASHIER"
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): User = User(
        userId = userId,
        username = username,
        pinHash = pinHash,
        role = if (role.equals("ADMIN", ignoreCase = true)) UserRole.ADMIN else UserRole.CASHIER,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            userId = user.userId,
            username = user.username,
            pinHash = user.pinHash,
            role = user.role.name,
            createdAt = user.createdAt
        )
    }
}
