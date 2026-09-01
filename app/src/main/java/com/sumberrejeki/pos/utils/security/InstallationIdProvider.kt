package com.sumberrejeki.pos.utils.security

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class InstallationIdProvider(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pos_device_identity", Context.MODE_PRIVATE)

    fun getInstallationId(): String {
        var id = prefs.getString(KEY_INSTALLATION_ID, null)
        if (id.isNullOrBlank()) {
            id = UUID.randomUUID().toString().uppercase()
            prefs.edit().putString(KEY_INSTALLATION_ID, id).apply()
        }
        return id
    }

    companion object {
        private const val KEY_INSTALLATION_ID = "installation_uuid"
    }
}
