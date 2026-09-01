package com.sumberrejeki.pos.domain.model

data class User(
    val userId: Long = 0,
    val username: String,
    val pinHash: String,
    val role: UserRole,
    val createdAt: Long = System.currentTimeMillis()
)
