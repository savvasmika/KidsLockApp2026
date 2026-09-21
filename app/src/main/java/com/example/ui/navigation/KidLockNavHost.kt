package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.KidLockApp
import com.example.model.ChildTheme
import com.example.model.DeviceRole
import com.example.model.LockState
import com.example.ui.child.ChildLockScreen
import com.example.ui.child.ChildUnlockedScreen
import com.example.ui.onboarding.ChildSetupScreen
import com.example.ui.onboarding.LanguageSelectionScreen
import com.example.ui.onboarding.ParentPinSetupScreen
import com.example.ui.onboarding.RoleSelectionScreen
import com.example.ui.onboarding.WelcomeScreen
import com.example.ui.pairing.ChildWaitingScreen
import com.example.ui.pairing.ParentPairingScreen
import com.example.ui.parent.DeviceSettingsScreen
import com.example.ui.parent.ParentDashboardScreen
import com.example.ui.parent.ParentPinDialog
import com.example.ui.parent.ThemeGalleryScreen
import kotlinx.coroutines.launch

@Composable
fun KidLockNavHost(
    navController: NavHostController,
    currentRole: DeviceRole,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = KidLockApp.instance
    val scope = rememberCoroutineScope()
    val activeTheme by app.settingsRepository.activeThemeFlow.collectAsState(initial = ChildTheme.SPACE)
    val childName by app.settingsRepository.childNameFlow.collectAsState(initial = "Giorgos")

    var showSwitchRolePinDialog by remember { mutableStateOf(false) }

    val startDestination = when (currentRole) {
        DeviceRole.PARENT -> Screen.ParentDashboard.route
        DeviceRole.CHILD -> Screen.ChildLock.route
        DeviceRole.UNSET -> Screen.Welcome.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Welcome Screen
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Screen.RoleSelection.route) },
                onLanguageClick = { navController.navigate(Screen.LanguageSelection.route) },
                currentLanguage = currentLanguage
            )
        }

        // 2. Role Selection
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onSelectRole = { role ->
                    scope.launch {
                        app.settingsRepository.setRole(role)
                        if (role == DeviceRole.PARENT) {
                            navController.navigate(Screen.ParentPinSetup.route)
                        } else {
                            navController.navigate(Screen.ChildSetup.route)
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 3. Language Selection
        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                currentLanguage = currentLanguage,
                onSelectLanguage = { lang ->
                    onLanguageChange(lang)
                },
                onContinue = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // 4. Parent PIN Setup
        composable(Screen.ParentPinSetup.route) {
            ParentPinSetupScreen(
                onPinCreated = { pin ->
                    scope.launch {
                        app.settingsRepository.setParentPin(pin)
                        navController.navigate(Screen.ParentPairing.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 5. Child Setup
        composable(Screen.ChildSetup.route) {
            ChildSetupScreen(
                initialName = childName,
                onComplete = { name, deviceType, theme ->
                    scope.launch {
                        app.settingsRepository.setChildProfile(name, deviceType)
                        app.settingsRepository.setActiveTheme(theme)
                        navController.navigate(Screen.ChildWaiting.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 6. Parent Pairing Screen
        composable(Screen.ParentPairing.route) {
            ParentPairingScreen(
                onPairingSuccess = { device ->
                    scope.launch {
                        app.deviceRepository.saveDevice(device)
                        navController.navigate(Screen.ParentDashboard.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 7. Child Waiting Screen
        composable(Screen.ChildWaiting.route) {
            ChildWaitingScreen(
                childName = childName,
                deviceType = "Tablet",
                onPairingAccepted = {
                    navController.navigate(Screen.ChildLock.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 8. Parent Dashboard
        composable(Screen.ParentDashboard.route) {
            ParentDashboardScreen(
                onNavigateSettings = { navController.navigate(Screen.DeviceSettings.route) },
                onNavigateThemes = { navController.navigate(Screen.ThemeGallery.route) },
                onNavigatePairNew = { navController.navigate(Screen.ParentPairing.route) },
                onSwitchRole = { showSwitchRolePinDialog = true },
                onLanguageClick = { navController.navigate(Screen.LanguageSelection.route) },
                currentLanguage = currentLanguage
            )
        }

        // 9. Child Lock Screen
        composable(Screen.ChildLock.route) {
            ChildLockScreen(
                childName = childName,
                onUnlocked = {
                    navController.navigate(Screen.ChildUnlocked.route) {
                        popUpTo(Screen.ChildLock.route) { inclusive = true }
                    }
                },
                onLanguageClick = { navController.navigate(Screen.LanguageSelection.route) },
                onThemesClick = { navController.navigate(Screen.ThemeGallery.route) },
                onSwitchRole = { showSwitchRolePinDialog = true },
                currentLanguage = currentLanguage
            )
        }

        // 10. Child Unlocked Screen
        composable(Screen.ChildUnlocked.route) {
            ChildUnlockedScreen(
                childName = childName,
                onLockRequested = {
                    navController.navigate(Screen.ChildLock.route) {
                        popUpTo(Screen.ChildUnlocked.route) { inclusive = true }
                    }
                },
                onThemesClick = { navController.navigate(Screen.ThemeGallery.route) },
                onParentSettings = { navController.navigate(Screen.DeviceSettings.route) }
            )
        }

        // 11. Device Settings Screen
        composable(Screen.DeviceSettings.route) {
            DeviceSettingsScreen(
                onBack = { navController.popBackStack() },
                onDeviceUnpaired = {
                    scope.launch {
                        app.settingsRepository.setRole(DeviceRole.UNSET)
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 12. Theme Gallery Screen
        composable(Screen.ThemeGallery.route) {
            ThemeGalleryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }

    if (showSwitchRolePinDialog) {
        ParentPinDialog(
            title = "Switch Device Role",
            subtitle = "Enter Parent PIN to switch between Parent and Child roles:",
            onPinSuccess = {
                showSwitchRolePinDialog = false
                scope.launch {
                    app.settingsRepository.setRole(DeviceRole.UNSET)
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            onDismiss = { showSwitchRolePinDialog = false }
        )
    }
}
