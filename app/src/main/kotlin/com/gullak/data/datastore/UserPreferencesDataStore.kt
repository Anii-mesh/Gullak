package com.animesh.gullak.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.get

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "finance_prefs")

data class UserPreferences(
    val userName: String,
    val currencySymbol: String,
    val isDarkMode: Boolean,
    val hasSeenOnboarding: Boolean
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            UserPreferences(
                userName = preferences[Keys.USER_NAME] ?: "Friend",
                currencySymbol = preferences[Keys.CURRENCY_SYMBOL] ?: "₹",
                isDarkMode = preferences[Keys.IS_DARK_MODE] ?: false,
                hasSeenOnboarding = preferences[Keys.HAS_SEEN_ONBOARDING] ?: false
            )
        }

    suspend fun updateUserName(name: String) {
        dataStore.edit { it[Keys.USER_NAME] = name }
    }

    suspend fun updateCurrencySymbol(symbol: String) {
        dataStore.edit { it[Keys.CURRENCY_SYMBOL] = symbol }
    }

    suspend fun toggleDarkMode() {
        dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = !(prefs[Keys.IS_DARK_MODE] ?: false)
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun markOnboardingComplete() {
        dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = true }
    }
}
