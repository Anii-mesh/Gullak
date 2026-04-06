package com.animesh.gullak.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animesh.gullak.data.datastore.UserPreferences
import com.animesh.gullak.data.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    val preferences = prefsDataStore.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences("Friend", "₹", false, false))

    fun updateUserName(name: String) {
        viewModelScope.launch { prefsDataStore.updateUserName(name) }
    }

    fun updateCurrencySymbol(symbol: String) {
        viewModelScope.launch { prefsDataStore.updateCurrencySymbol(symbol) }
    }

    fun toggleDarkMode() {
        viewModelScope.launch { prefsDataStore.toggleDarkMode() }
    }
}
