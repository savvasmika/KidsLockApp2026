package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.model.ChildTheme
import com.example.model.DeviceRole
import com.example.ui.navigation.KidLockNavHost
import com.example.ui.theme.KidLockTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = KidLockApp.instance
            val scope = rememberCoroutineScope()
            val currentRole by app.settingsRepository.roleFlow.collectAsState(initial = DeviceRole.UNSET)
            val currentLanguage by app.settingsRepository.languageFlow.collectAsState(initial = "en")
            val activeTheme by app.settingsRepository.activeThemeFlow.collectAsState(initial = ChildTheme.SPACE)

            // Localized Context provider for real-time in-app language switching
            val localizedContext = remember(currentLanguage) {
                createLocalizedContext(this, currentLanguage)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                val isChildMode = currentRole == DeviceRole.CHILD
                KidLockTheme(
                    isChildMode = isChildMode,
                    childTheme = activeTheme
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        KidLockNavHost(
                            navController = navController,
                            currentRole = currentRole,
                            currentLanguage = currentLanguage,
                            onLanguageChange = { newLang ->
                                scope.launch {
                                    app.settingsRepository.setLanguage(newLang)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun createLocalizedContext(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
