package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object RoleSelection : Screen("role_selection")
    object LanguageSelection : Screen("language_selection")
    object ParentPinSetup : Screen("parent_pin_setup")
    object ChildSetup : Screen("child_setup")
    object ParentPairing : Screen("parent_pairing")
    object ChildWaiting : Screen("child_waiting")
    object ParentDashboard : Screen("parent_dashboard")
    object ChildLock : Screen("child_lock")
    object ChildUnlocked : Screen("child_unlocked")
    object DeviceSettings : Screen("device_settings")
    object ThemeGallery : Screen("theme_gallery")
}
