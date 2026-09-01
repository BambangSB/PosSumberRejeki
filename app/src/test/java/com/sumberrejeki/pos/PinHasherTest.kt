package com.sumberrejeki.pos

import com.sumberrejeki.pos.utils.security.PinHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    @Test
    fun `hashPin generates non-empty consistent hash`() {
        val pin = "123456"
        val hash1 = PinHasher.hashPin(pin)
        val hash2 = PinHasher.hashPin(pin)

        assertTrue(hash1.isNotEmpty())
        assertTrue(hash1 == hash2)
    }

    @Test
    fun `verifyPin correctly validates matching and mismatched PINs`() {
        val pin = "889900"
        val hash = PinHasher.hashPin(pin)

        assertTrue(PinHasher.verifyPin("889900", hash))
        assertFalse(PinHasher.verifyPin("889901", hash))
        assertFalse(PinHasher.verifyPin("000000", hash))
    }

    @Test
    fun `different PINs produce different hashes`() {
        val hash1 = PinHasher.hashPin("111111")
        val hash2 = PinHasher.hashPin("222222")

        assertNotEquals(hash1, hash2)
    }
}
