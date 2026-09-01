package com.sumberrejeki.pos

import com.sumberrejeki.pos.utils.security.Base64Compat
import com.sumberrejeki.pos.utils.security.RsaKeyHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

class VerifyRsaLicenseUseCaseTest {

    private fun getPublicKey(): PublicKey {
        val keyBytes = Base64Compat.decode(RsaKeyHelper.BUNDLED_PUBLIC_KEY_B64)
        val spec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    @Test
    fun `verify valid license with matching installation ID succeeds`() {
        val installationId = "DEVICE-POS-UUID-9999"
        val licenseKey = RsaKeyHelper.generateLicense(
            installationId = installationId,
            licensedTo = "Toko Berkah Sejahtera",
            tier = "PREMIUM"
        )

        val pubKey = getPublicKey()
        val result = RsaKeyHelper.verifyLicense(licenseKey, installationId, pubKey)

        assertTrue(result.isSuccess)
        val payload = result.getOrNull()!!
        assertEquals(installationId, payload.installationId)
        assertEquals("Toko Berkah Sejahtera", payload.licensedTo)
        assertEquals("PREMIUM", payload.tier)
    }

    @Test
    fun `verify license with different device installation ID fails`() {
        val myDeviceId = "DEVICE-A"
        val otherDeviceId = "DEVICE-B"

        val licenseForOtherDevice = RsaKeyHelper.generateLicense(
            installationId = otherDeviceId,
            licensedTo = "Toko Lain"
        )

        val pubKey = getPublicKey()
        val result = RsaKeyHelper.verifyLicense(licenseForOtherDevice, myDeviceId, pubKey)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("perangkat lain") == true)
    }

    @Test
    fun `tampered license payload fails signature verification`() {
        val installationId = "DEVICE-TAMPER-1"
        val originalLicense = RsaKeyHelper.generateLicense(installationId = installationId)
        val parts = originalLicense.split(".")

        // Modify the payload slightly
        val tamperedPayload = parts[0] + "A"
        val tamperedLicense = "$tamperedPayload.${parts[1]}"

        val pubKey = getPublicKey()
        val result = RsaKeyHelper.verifyLicense(tamperedLicense, installationId, pubKey)

        assertFalse(result.isSuccess)
    }
}
