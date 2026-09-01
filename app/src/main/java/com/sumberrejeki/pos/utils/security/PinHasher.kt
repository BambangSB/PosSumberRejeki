package com.sumberrejeki.pos.utils.security

import java.security.MessageDigest

object PinHasher {
    private const val SALT = "sumber_rejeki_pos_salt_2026"

    fun hashPin(pin: String): String {
        val salted = "$SALT:$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(salted.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, hash: String): Boolean {
        return hashPin(pin).equals(hash, ignoreCase = true)
    }
}
