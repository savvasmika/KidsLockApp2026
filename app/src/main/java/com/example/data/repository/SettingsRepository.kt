package com.example.data.repository

import com.example.data.local.dao.AppConfigDao
import com.example.data.local.entity.AppConfigEntity
import com.example.model.ChildTheme
import com.example.model.DeviceRole
import com.example.security.CryptoUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val appConfigDao: AppConfigDao) {

    companion object {
        private const val KEY_ROLE = "device_role"
        private const val KEY_PARENT_PIN_HASH = "parent_pin_hash"
        private const val KEY_MY_DEVICE_ID = "my_device_id"
        private const val KEY_CHILD_NAME = "child_name"
        private const val KEY_DEVICE_TYPE = "device_type"
        private const val KEY_ACTIVE_THEME = "active_theme"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_ANIMATIONS_ENABLED = "animations_enabled"
        private const val KEY_INACTIVITY_MINUTES = "inactivity_minutes"
        private const val KEY_KIOSK_PINNED = "kiosk_pinned"
    }

    val roleFlow: Flow<DeviceRole> = appConfigDao.getConfigFlow(KEY_ROLE).map {
        when (it) {
            "PARENT" -> DeviceRole.PARENT
            "CHILD" -> DeviceRole.CHILD
            else -> DeviceRole.UNSET
        }
    }

    val childNameFlow: Flow<String> = appConfigDao.getConfigFlow(KEY_CHILD_NAME).map {
        it ?: "Giorgos"
    }

    val activeThemeFlow: Flow<ChildTheme> = appConfigDao.getConfigFlow(KEY_ACTIVE_THEME).map {
        ChildTheme.fromId(it)
    }

    val languageFlow: Flow<String> = appConfigDao.getConfigFlow(KEY_LANGUAGE).map {
        it ?: "en"
    }

    val animationsEnabledFlow: Flow<Boolean> = appConfigDao.getConfigFlow(KEY_ANIMATIONS_ENABLED).map {
        it?.toBooleanStrictOrNull() ?: true
    }

    val inactivityMinutesFlow: Flow<Int> = appConfigDao.getConfigFlow(KEY_INACTIVITY_MINUTES).map {
        it?.toIntOrNull() ?: 15
    }

    val isKioskPinnedFlow: Flow<Boolean> = appConfigDao.getConfigFlow(KEY_KIOSK_PINNED).map {
        it?.toBooleanStrictOrNull() ?: false
    }

    suspend fun getMyDeviceId(): String {
        var id = appConfigDao.getConfigValue(KEY_MY_DEVICE_ID)
        if (id.isNullOrBlank()) {
            id = "kl_" + CryptoUtils.generateSecureToken(8)
            appConfigDao.setConfig(AppConfigEntity(KEY_MY_DEVICE_ID, id))
        }
        return id
    }

    suspend fun getRole(): DeviceRole {
        return when (appConfigDao.getConfigValue(KEY_ROLE)) {
            "PARENT" -> DeviceRole.PARENT
            "CHILD" -> DeviceRole.CHILD
            else -> DeviceRole.UNSET
        }
    }

    suspend fun setRole(role: DeviceRole) {
        appConfigDao.setConfig(AppConfigEntity(KEY_ROLE, role.name))
    }

    suspend fun setParentPin(pin: String) {
        val hash = CryptoUtils.hashPin(pin)
        appConfigDao.setConfig(AppConfigEntity(KEY_PARENT_PIN_HASH, hash))
    }

    suspend fun verifyParentPin(pin: String): Boolean {
        val storedHash = appConfigDao.getConfigValue(KEY_PARENT_PIN_HASH) ?: return false
        return CryptoUtils.verifyPin(pin, storedHash)
    }

    suspend fun hasParentPin(): Boolean {
        return !appConfigDao.getConfigValue(KEY_PARENT_PIN_HASH).isNullOrBlank()
    }

    suspend fun setChildProfile(name: String, deviceType: String) {
        appConfigDao.setConfig(AppConfigEntity(KEY_CHILD_NAME, name))
        appConfigDao.setConfig(AppConfigEntity(KEY_DEVICE_TYPE, deviceType))
    }

    suspend fun getChildName(): String {
        return appConfigDao.getConfigValue(KEY_CHILD_NAME) ?: "Giorgos"
    }

    suspend fun getDeviceType(): String {
        return appConfigDao.getConfigValue(KEY_DEVICE_TYPE) ?: "Tablet"
    }

    suspend fun setActiveTheme(theme: ChildTheme) {
        appConfigDao.setConfig(AppConfigEntity(KEY_ACTIVE_THEME, theme.id))
    }

    suspend fun setLanguage(langCode: String) {
        appConfigDao.setConfig(AppConfigEntity(KEY_LANGUAGE, langCode))
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        appConfigDao.setConfig(AppConfigEntity(KEY_ANIMATIONS_ENABLED, enabled.toString()))
    }

    suspend fun setInactivityMinutes(minutes: Int) {
        appConfigDao.setConfig(AppConfigEntity(KEY_INACTIVITY_MINUTES, minutes.toString()))
    }

    suspend fun setKioskPinned(pinned: Boolean) {
        appConfigDao.setConfig(AppConfigEntity(KEY_KIOSK_PINNED, pinned.toString()))
    }

    suspend fun resetAll() {
        appConfigDao.clearAll()
    }
}
