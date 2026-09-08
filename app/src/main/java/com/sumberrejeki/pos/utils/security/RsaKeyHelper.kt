package com.sumberrejeki.pos.utils.security

import android.content.Context
import com.sumberrejeki.pos.R
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

object Base64Compat {
    fun encodeToString(data: ByteArray): String {
        return try {
            val clazz = Class.forName("android.util.Base64")
            val method = clazz.getMethod("encodeToString", ByteArray::class.java, Int::class.javaPrimitiveType)
            method.invoke(null, data, 2) as String // 2 = NO_WRAP
        } catch (e: Throwable) {
            java.util.Base64.getEncoder().encodeToString(data)
        }
    }

    fun decode(str: String): ByteArray {
        return try {
            val clazz = Class.forName("android.util.Base64")
            val method = clazz.getMethod("decode", String::class.java, Int::class.javaPrimitiveType)
            method.invoke(null, str, 0) as ByteArray // 0 = DEFAULT
        } catch (e: Throwable) {
            java.util.Base64.getDecoder().decode(str.trim())
        }
    }
}


data class LicensePayload(
    val installationId: String,
    val licensedTo: String,
    val tier: String,
    val issuedAt: Long,
    val expiresAt: Long?
)

object RsaKeyHelper {

    private const val ALGORITHM_RSA = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA256withRSA"

    /**
     * [PRODUKSI] Public Key RSA-2048 yang di-embed di aplikasi.
     * Di-generate: 2026-09-08. Pasangan dari production_private_key.b64 di folder tools/.
     * JANGAN ganti nilai ini kecuali Anda juga mengganti private key di License Generator.
     */
    const val BUNDLED_PUBLIC_KEY_B64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA57KgAPeuGW4w9P+fN7Y/39Mq52CJWZPekpUelKBoz4aOf25qdeGf9ovVr/9Fev5Lno2qbDRvYnOF0R0jNXdHKtwiDY2UAPePdxLHtCZWAWYWYZ6kZSE2CAdcaKJgCFE9xtkbSb/9gXf8ElpUkySDdnpSFBEuq/447/RD1CEPTwKEUPKUOpShhcNDqQ1fu9/2nxbIiuqj0BJ5goAvhyumjg0j84gih3Un3bkER7xpx76UvZM5Pa4LGgaxrazLQU8e8xl+GfuvfFFPh8WQxkfNwGoKw4mZdTLWujRv8AJFw6vtpBjBJCg0xeqvD9ZiSwemna3V7CgNVu0u/SEfthoTVwIDAQAB"


    fun loadPublicKeyFromRawResource(context: Context): PublicKey {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.public_key)
            val keyBytes = inputStream.readBytes()
            inputStream.close()
            val spec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance(ALGORITHM_RSA)
            keyFactory.generatePublic(spec)
        } catch (e: Exception) {
            // Fallback to bundled constant if resource loading fails
            val keyBytes = Base64Compat.decode(BUNDLED_PUBLIC_KEY_B64)
            val spec = X509EncodedKeySpec(keyBytes)
            KeyFactory.getInstance(ALGORITHM_RSA).generatePublic(spec)
        }
    }

    fun verifyLicense(
        licenseString: String,
        currentInstallationId: String,
        publicKey: PublicKey
    ): Result<LicensePayload> {
        return try {
            val parts = licenseString.trim().split(".")
            if (parts.size != 2) {
                return Result.failure(IllegalArgumentException("Format lisensi tidak valid (harus Payload.Signature)"))
            }

            val payloadB64 = parts[0]
            val signatureB64 = parts[1]

            val payloadBytes = Base64Compat.decode(payloadB64)
            val signatureBytes = Base64Compat.decode(signatureB64)

            // Verify RSA Signature
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
            sig.initVerify(publicKey)
            sig.update(payloadBytes)

            if (!sig.verify(signatureBytes)) {
                return Result.failure(SecurityException("Tanda tangan lisensi tidak valid atau telah dimodifikasi"))
            }

            // Parse Payload JSON
            val jsonString = String(payloadBytes, Charsets.UTF_8)
            val json = JSONObject(jsonString)

            val installationId = json.getString("installationId")
            val licensedTo = json.optString("licensedTo", "Sumber Rejeki POS")
            val tier = json.optString("tier", "PREMIUM")
            val issuedAt = json.optLong("issuedAt", System.currentTimeMillis())
            val expiresAt = if (json.has("expiresAt")) json.getLong("expiresAt") else null

            // Validate installation ID matches current device
            if (!installationId.equals(currentInstallationId, ignoreCase = true)) {
                return Result.failure(IllegalStateException("Lisensi ini dibuat untuk perangkat lain ($installationId)"))
            }

            // Check expiration if specified
            if (expiresAt != null && expiresAt < System.currentTimeMillis()) {
                return Result.failure(IllegalStateException("Lisensi ini sudah kedaluwarsa"))
            }

            Result.success(
                LicensePayload(
                    installationId = installationId,
                    licensedTo = licensedTo,
                    tier = tier,
                    issuedAt = issuedAt,
                    expiresAt = expiresAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
