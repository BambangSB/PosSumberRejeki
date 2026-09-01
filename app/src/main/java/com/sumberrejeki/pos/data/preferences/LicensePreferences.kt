package com.sumberrejeki.pos.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sumberrejeki.pos.domain.model.LicenseInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.licenseDataStore: DataStore<Preferences> by preferencesDataStore(name = "license_preferences")

class LicensePreferences(private val context: Context) {

    private object PreferencesKeys {
        val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        val KEY_LICENSE_KEY = stringPreferencesKey("license_key")
        val KEY_LICENSED_TO = stringPreferencesKey("licensed_to")
        val KEY_ACTIVATION_DATE = longPreferencesKey("activation_date")
    }

    val licenseInfoFlow: Flow<LicenseInfo> = context.licenseDataStore.data.map { preferences ->
        val isPremium = preferences[PreferencesKeys.KEY_IS_PREMIUM] ?: false
        val licenseKey = preferences[PreferencesKeys.KEY_LICENSE_KEY] ?: ""
        val licensedTo = preferences[PreferencesKeys.KEY_LICENSED_TO] ?: "Sumber Rejeki POS"
        val activationDate = preferences[PreferencesKeys.KEY_ACTIVATION_DATE]
        LicenseInfo(
            isPremium = isPremium,
            licensedTo = licensedTo,
            licenseKey = licenseKey,
            activationDate = activationDate,
            maxUsers = if (isPremium) 3 else 1
        )
    }

    suspend fun saveLicense(isPremium: Boolean, licenseKey: String, licensedTo: String, activationDate: Long) {
        context.licenseDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_PREMIUM] = isPremium
            preferences[PreferencesKeys.KEY_LICENSE_KEY] = licenseKey
            preferences[PreferencesKeys.KEY_LICENSED_TO] = licensedTo
            preferences[PreferencesKeys.KEY_ACTIVATION_DATE] = activationDate
        }
    }

    suspend fun clearLicense() {
        context.licenseDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_PREMIUM] = false
            preferences[PreferencesKeys.KEY_LICENSE_KEY] = ""
            preferences[PreferencesKeys.KEY_LICENSED_TO] = "Sumber Rejeki POS"
            preferences.remove(PreferencesKeys.KEY_ACTIVATION_DATE)
        }
    }
}
