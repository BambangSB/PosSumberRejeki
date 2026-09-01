package com.sumberrejeki.pos.utils.security

import android.content.Context
import com.sumberrejeki.pos.R
import org.json.JSONObject
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
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

    // Default bundled fallback public key DER bytes (matches the generated keypair)
    const val BUNDLED_PUBLIC_KEY_B64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwtF90HpSioIryYSSaQxqvuBzUhUR9cjaVFYLs1jgdCZ47ublfWjyUHk/RfoVwrnaWA57dtybacOuaghuDDz2FGf1VD+8q/Z+IL6M6rlS5koong0w8edmuA6dZPjqMQXrd2hbV5GIZdgJMZHrEG39Ip61VnO6E0i9PBfbhHxamnR3baHgXyXynOcY+Krkbav11VrDeXdckmij+IMH9TKeqmvBr7ZIMFIvDjEY/M/7HX8C1pRRw2kflaGfrzYsV/IcHZ3W7s9CHTJ2/0b9xz81uI9iB5smAmcD3i9q1jAPSskayst26OXtDF7JS0gk6P24I3U2/J00REMS7dujhhMVVQIDAQAB"

    // Optional private key for generating valid license keys in testing / demo mode
    const val DEMO_PRIVATE_KEY_B64 = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDC0X3QelKKgivJhJJpDGq+4HNSFRH1yNpUVguzWOB0Jnju5uV9aPJQeT9F+hXCudpYDnt23Jtpw65qCG4MPPYUZ/VUP7yr9n4gvozquVLmSiieDTDx52a4Dp1k+OoxBet3aFtXkYhl2AkxkesQbf0inrVWc7oTSL08F9uEfFqadHdtoeBfJfKc5xj4quRtq/XVWsN5d1ySaKP4gwf1Mp6qa8GvtkgwUi8OMRj8z/sdfwLWlFHDaR+VoZ+vNixX8hwdndbuz0IdMnb/Rv3HPzW4j2IHmyYCZwPeL2rWMA9KyRrKy3bo5e0MXslLSCTo/bgjdTb8nTREQxLt26OGExVVAgMBAAECggEAIqgWzB/+kAJBHNu3GfXqBWOzUB5McB9Xa5pVAYYlg7QWXlV37LvfI162WVH4Tgg25eRJpWbwRLvZSqaOQSIVGqM1utON0j+Kx3NuaREnxKzbleY910K1fLeo2z4Gl0r4zOilGypaC9gHJqjRGr6eQere0m7K4sRVWZva4rImggQ3fFK9t+Oa0MQJZMxpSZjHKmtyDuzb88YzS0OIBkwHAvUfFwxEq9uLAH3dTCX9FGGzTniGLl6XesB6ju53FriksBsKJcrXJLd+k/OBBrWgTmvt8beD5jcQpZaiJ9WO2ed/953iBkRoGYVch8H4fm9C5PIGe+ark4B+oOgdMD1f3wKBgQDX60l+ff3CBc7kVrHa7VvQUiik220Kv/F0sfCxVAkcLO83NUqx3rtSlR1KZvPItWu0qsvFnIWI3AJ13/tCYuF5ddHpYYonv02iI6q639eAmV/0al6X+REUHY5cry0t637Vxn/aqQTuZKXf+o2Gu980DKBQcyZGNSlcLBd/0/+WFwKBgQDm+3kLVtti69tHa+1Jnnt0EE7RydDbjVwTY7bLMAVvwpja4rEoMgfW4kKdldfHPgTt9+C7KrQpjV2ntX6cVLLuPb6MwK91hmz86/1LlW6Do02Av2HqCVCEEOFaxr3fX/aOgib4TlyiKy/kE2Cus69B5D3cxzUtL2NyXsMdxSU/cwKBgF1YpjxeYc94zVogNOIODcyBSHsFNh4Yn12w8/rl9s5MitrWWkWNonRwbOpWmfximn7P/CqhoYVqgFHPlIO91fa5vhdkiOLoLLr1KoGB9lvtcvNq7RxMnFqS8JIXVbPthLcNCwdQXQZnUuMDIh5mqHeLId/F+H0PzKLBmSM1VuDfAoGABFTg7zqAwLklkpBOkdvb+tA3ei2rsgJToLV0/xh2caMXlLs9kUBm7zus/iGKO11JYGEOoNPrLwBMqlF3/1yDQn577U3LvIt5zcBrLpityuAsGub74hSsot1iLH6fnunAyOld67gkepIOwtsuK/iIp9as5lACCkfiOInYZN2GlksCgYA5jFKWmUMw7zYEzfg0RaTxHxM+j0O+WKpYx6GSDYsDgL6uNUsSgCdp1+Vz+Tn/RMe9wI6RdheAOv0TveKVb0X0YOORAVOpIw7mdRSiDYL9/MZmNEupj5S5Ei/uiWKWdleTKFsMStzVn8C9/ve/2lbBiratjtkzCigjgPRYOcFrog=="

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

    /**
     * Generates a signed license string for a given installation ID.
     * Useful for developer activation, offline key generator tool, and automated tests.
     */
    fun generateLicense(
        installationId: String,
        licensedTo: String = "Toko Sumber Rejeki",
        tier: String = "PREMIUM",
        expiresAt: Long? = null,
        privateKeyB64: String = DEMO_PRIVATE_KEY_B64
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

        val privKeyBytes = Base64Compat.decode(privateKeyB64)
        val keySpec = PKCS8EncodedKeySpec(privKeyBytes)
        val keyFactory = KeyFactory.getInstance(ALGORITHM_RSA)
        val privateKey: PrivateKey = keyFactory.generatePrivate(keySpec)

        val sig = Signature.getInstance(SIGNATURE_ALGORITHM)
        sig.initSign(privateKey)
        sig.update(payloadBytes)
        val signatureBytes = sig.sign()
        val signatureB64 = Base64Compat.encodeToString(signatureBytes)

        return "$payloadB64.$signatureB64"
    }
}
