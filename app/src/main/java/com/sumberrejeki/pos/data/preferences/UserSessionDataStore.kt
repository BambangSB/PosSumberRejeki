package com.sumberrejeki.pos.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sumberrejeki.pos.domain.model.User
import com.sumberrejeki.pos.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserSessionDataStore(private val context: Context) {

    private object PreferencesKeys {
        val KEY_USER_ID = longPreferencesKey("session_user_id")
        val KEY_USERNAME = stringPreferencesKey("session_username")
        val KEY_USER_ROLE = stringPreferencesKey("session_user_role")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("session_is_logged_in")
        val KEY_LOGIN_TIME = longPreferencesKey("session_login_time")
    }

    val currentSessionFlow: Flow<User?> = context.sessionDataStore.data.map { preferences ->
        val isLoggedIn = preferences[PreferencesKeys.KEY_IS_LOGGED_IN] ?: false
        if (!isLoggedIn) {
            null
        } else {
            val userId = preferences[PreferencesKeys.KEY_USER_ID] ?: 0L
            val username = preferences[PreferencesKeys.KEY_USERNAME] ?: ""
            val roleStr = preferences[PreferencesKeys.KEY_USER_ROLE] ?: UserRole.CASHIER.name
            val role = if (roleStr.equals(UserRole.ADMIN.name, ignoreCase = true)) UserRole.ADMIN else UserRole.CASHIER
            User(
                userId = userId,
                username = username,
                pinHash = "",
                role = role,
                createdAt = preferences[PreferencesKeys.KEY_LOGIN_TIME] ?: 0L
            )
        }
    }

    suspend fun saveSession(user: User) {
        context.sessionDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_USER_ID] = user.userId
            preferences[PreferencesKeys.KEY_USERNAME] = user.username
            preferences[PreferencesKeys.KEY_USER_ROLE] = user.role.name
            preferences[PreferencesKeys.KEY_IS_LOGGED_IN] = true
            preferences[PreferencesKeys.KEY_LOGIN_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences[PreferencesKeys.KEY_IS_LOGGED_IN] = false
            preferences.remove(PreferencesKeys.KEY_USER_ID)
            preferences.remove(PreferencesKeys.KEY_USERNAME)
            preferences.remove(PreferencesKeys.KEY_USER_ROLE)
            preferences.remove(PreferencesKeys.KEY_LOGIN_TIME)
        }
    }
}
