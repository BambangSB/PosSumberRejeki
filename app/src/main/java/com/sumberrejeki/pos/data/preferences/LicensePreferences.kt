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
        val KEY_EXPIRES_AT = longPreferencesKey("expires_at")
        val KEY_FIRST_INSTALL_TIME = longPreferencesKey("first_install_time")
    }

    companion object {
        const val TRIAL_DURATION_MILLIS = 3L * 24 * 60 * 60 * 1000L // 3 Hari (72 Jam)
    }

    val licenseInfoFlow: Flow<LicenseInfo> = context.licenseDataStore.data.map { preferences ->
        val rawIsPremium = preferences[PreferencesKeys.KEY_IS_PREMIUM] ?: false
        val licenseKey = preferences[PreferencesKeys.KEY_LICENSE_KEY] ?: ""
        val licensedTo = preferences[PreferencesKeys.KEY_LICENSED_TO] ?: "Sumber Rejeki POS"
        val activationDate = preferences[PreferencesKeys.KEY_ACTIVATION_DATE]
        val expiresAt = preferences[PreferencesKeys.KEY_EXPIRES_AT]
        val firstInstallTime = preferences[PreferencesKeys.KEY_FIRST_INSTALL_TIME]

        val now = System.currentTimeMillis()

        // Check if premium license is still valid or expired
        val isPremiumValid = if (rawIsPremium) {
            if (expiresAt != null && expiresAt > 0L) now <= expiresAt else true
        } else {
            false
        }

        // Calculate 3-day Free Pro trial for new installation
        val installTime = firstInstallTime ?: now
        val trialEndTime = installTime + TRIAL_DURATION_MILLIS
        val isTrialActive = !isPremiumValid && (now < trialEndTime)

        val remainingMillis = if (isTrialActive) (trialEndTime - now).coerceAtLeast(0L) else 0L
        val trialDaysRemaining = if (isTrialActive) {
            ((remainingMillis + 86399999L) / (24 * 60 * 60 * 1000L)).toInt()
        } else {
            0
        }
        val trialHoursRemaining = if (isTrialActive) {
            (remainingMillis / (60 * 60 * 1000L)).toInt()
        } else {
            0
        }

        LicenseInfo(
            isPremium = isPremiumValid,
            isTrialActive = isTrialActive,
            trialDaysRemaining = trialDaysRemaining,
            trialHoursRemaining = trialHoursRemaining,
            firstInstallTime = installTime,
            expiresAt = expiresAt,
            licensedTo = licensedTo,
            licenseKey = licenseKey,
            activationDate = activationDate
        )
    }

    suspend fun ensureFirstInstallTimeInitialized() {
        context.licenseDataStore.edit { preferences ->
            if (preferences[PreferencesKeys.KEY_FIRST_INSTALL_TIME] == null) {
                preferences[PreferencesKeys.KEY_FIRST_INSTALL_TIME] = System.currentTimeMillis()
            }
        }
    }

    suspend fun saveLicense(
        isPremium: Boolean,
        licenseKey: String,
        licensedTo: String,
        activationDate: Long,
        expiresAt: Long? = null
    ) {
        context.licenseDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_PREMIUM] = isPremium
            preferences[PreferencesKeys.KEY_LICENSE_KEY] = licenseKey
            preferences[PreferencesKeys.KEY_LICENSED_TO] = licensedTo
            preferences[PreferencesKeys.KEY_ACTIVATION_DATE] = activationDate
            if (expiresAt != null && expiresAt > 0L) {
                preferences[PreferencesKeys.KEY_EXPIRES_AT] = expiresAt
            } else {
                preferences.remove(PreferencesKeys.KEY_EXPIRES_AT)
            }
        }
    }

    suspend fun updateStoreName(newStoreName: String) {
        context.licenseDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_LICENSED_TO] = newStoreName
        }
    }

    suspend fun clearLicense() {
        context.licenseDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_PREMIUM] = false
            preferences[PreferencesKeys.KEY_LICENSE_KEY] = ""
            preferences[PreferencesKeys.KEY_LICENSED_TO] = "Sumber Rejeki POS"
            preferences.remove(PreferencesKeys.KEY_ACTIVATION_DATE)
            preferences.remove(PreferencesKeys.KEY_EXPIRES_AT)
        }
    }
}

