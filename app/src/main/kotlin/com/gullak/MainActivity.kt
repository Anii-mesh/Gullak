package com.animesh.gullak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.animesh.gullak.ui.FinanceNavHost
import com.animesh.gullak.ui.profile.ProfileViewModel
import com.animesh.gullak.ui.theme.FinanceAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val prefs by profileViewModel.preferences.collectAsStateWithLifecycle()
            FinanceAppTheme(darkTheme = prefs.isDarkMode) {
                FinanceNavHost()
            }
        }
    }
}
