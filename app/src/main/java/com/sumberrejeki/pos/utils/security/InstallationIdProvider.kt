package com.sumberrejeki.pos.utils.security

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.util.UUID

/**
 * Menyediakan Installation ID yang stabil untuk perangkat ini.
 *
 * Strategi (urutan prioritas):
 * 1. ANDROID_ID: Identifier unik per (perangkat + signing key) dari Android OS.
 *    - TIDAK memerlukan permission apapun (berbeda dengan IMEI yang butuh READ_PHONE_STATE).
 *    - Persisten meski app di-uninstall/re-install — inilah yang memecahkan bug re-install.
 *    - Hanya berubah jika factory reset atau app signing key berubah.
 * 2. UUID Fallback: Jika ANDROID_ID tidak tersedia (edge case emulator/tertentu),
 *    generate UUID sekali dan simpan di SharedPreferences.
 *    Meski kurang persisten dari ANDROID_ID, ini adalah safety net yang baik.
 *
 * Referensi: https://developer.android.com/training/articles/user-data-ids
 */
class InstallationIdProvider(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pos_device_identity", Context.MODE_PRIVATE)

    fun getInstallationId(): String {
        // Cek cache dulu untuk performa
        val cached = prefs.getString(KEY_INSTALLATION_ID, null)
        if (!cached.isNullOrBlank()) return cached

        // Utamakan ANDROID_ID — persisten & tidak butuh permission berbahaya
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val id = if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            // "9774d56d682e549c" adalah ANDROID_ID default emulator/factory lama — skip
            // Format: 8 karakter hex uppercase, mudah dibaca dan diketik
            androidId.uppercase().take(8)
        } else {
            // Fallback: UUID random, simpan permanen agar konsisten
            UUID.randomUUID().toString().replace("-", "").uppercase().take(8)
        }

        prefs.edit().putString(KEY_INSTALLATION_ID, id).apply()
        return id
    }

    companion object {
        private const val KEY_INSTALLATION_ID = "installation_uuid"
    }
}
