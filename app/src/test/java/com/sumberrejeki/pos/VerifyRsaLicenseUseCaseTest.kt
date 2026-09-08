package com.sumberrejeki.pos

import com.sumberrejeki.pos.utils.security.Base64Compat
import com.sumberrejeki.pos.utils.security.RsaKeyHelper
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class VerifyRsaLicenseUseCaseTest {

    companion object {
        // Ephemeral RSA-2048 keypair for isolated unit testing
        private val testKeyPair: KeyPair by lazy {
            val kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(2048)
            kpg.generateKeyPair()
        }

        private fun generateTestLicense(
            installationId: String,
            licensedTo: String = "Toko Berkah Sejahtera",
            tier: String = "PREMIUM",
            expiresAt: Long? = null,
            privateKey: PrivateKey = testKeyPair.private
        ): String {
            val json = JSONObject().apply {
                put("installationId", installationId.trim().uppercase())
                put("licensedTo", licensedTo)
                put("tier", tier)
                put("issuedAt", System.currentTimeMillis())
                if (expiresAt != null) {
                    put("expiresAt", expiresAt)
                }
            }

            val payloadBytes = json.toString().toByteArray(Charsets.UTF_8)
            val payloadB64 = Base64Compat.encodeToString(payloadBytes)

            val sig = Signature.getInstance("SHA256withRSA")
            sig.initSign(privateKey)
            sig.update(payloadBytes)
            val signatureBytes = sig.sign()
            val signatureB64 = Base64Compat.encodeToString(signatureBytes)

            return "$payloadB64.$signatureB64"
        }
    }

    private fun getBundledPublicKey(): PublicKey {
        val keyBytes = Base64Compat.decode(RsaKeyHelper.BUNDLED_PUBLIC_KEY_B64)
        val spec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    @Test
    fun `bundled public key is valid RSA-2048 format`() {
        val pubKey = getBundledPublicKey()
        assertNotNull(pubKey)
        assertEquals("RSA", pubKey.algorithm)
    }

    @Test
    fun `verify valid license with matching installation ID succeeds`() {
        val installationId = "A1B2C3D4"
        val licenseKey = generateTestLicense(
            installationId = installationId,
            licensedTo = "Toko Berkah Sejahtera",
            tier = "PREMIUM"
        )

        val result = RsaKeyHelper.verifyLicense(licenseKey, installationId, testKeyPair.public)

        assertTrue(result.isSuccess)
        val payload = result.getOrNull()!!
        assertEquals(installationId, payload.installationId)
        assertEquals("Toko Berkah Sejahtera", payload.licensedTo)
        assertEquals("PREMIUM", payload.tier)
    }

    @Test
    fun `verify license with different device installation ID fails`() {
        val myDeviceId = "A1B2C3D4"
        val otherDeviceId = "X9Y8Z7W6"

        val licenseForOtherDevice = generateTestLicense(
            installationId = otherDeviceId,
            licensedTo = "Toko Lain"
        )

        val result = RsaKeyHelper.verifyLicense(licenseForOtherDevice, myDeviceId, testKeyPair.public)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("perangkat lain") == true)
    }

    @Test
    fun `tampered license payload fails signature verification`() {
        val installationId = "A1B2C3D4"
        val originalLicense = generateTestLicense(installationId = installationId)
        val parts = originalLicense.split(".")

        // Modify the payload slightly
        val tamperedPayload = parts[0] + "A"
        val tamperedLicense = "$tamperedPayload.${parts[1]}"

        val result = RsaKeyHelper.verifyLicense(tamperedLicense, installationId, testKeyPair.public)

        assertFalse(result.isSuccess)
    }

    @Test
    fun `expired license fails verification`() {
        val installationId = "A1B2C3D4"
        val pastTime = System.currentTimeMillis() - 100000 // already expired
        val expiredLicense = generateTestLicense(
            installationId = installationId,
            expiresAt = pastTime
        )

        val result = RsaKeyHelper.verifyLicense(expiredLicense, installationId, testKeyPair.public)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("kedaluwarsa") == true)
    }
}
