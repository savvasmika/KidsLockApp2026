package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.local.KidLockDatabase
import com.example.data.repository.DeviceRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.UnlockRepository
import com.example.network.LocalClient
import com.example.network.NsdHelper
import com.example.network.RemoteBackendService
import com.example.network.RemoteBackendServiceImpl

class KidLockApp : Application() {

    companion object {
        const val CHANNEL_ID_UNLOCK = "kidlock_unlock_channel"
        const val CHANNEL_ID_SECURITY = "kidlock_security_channel"
        lateinit var instance: KidLockApp
            private set
    }

    val database by lazy { KidLockDatabase.getDatabase(this) }
    val settingsRepository by lazy { SettingsRepository(database.appConfigDao()) }
    val deviceRepository by lazy { DeviceRepository(database.deviceDao()) }
    val unlockRepository by lazy { UnlockRepository(database.unlockDao()) }
    val nsdHelper by lazy { NsdHelper(this) }
    val localClient by lazy { LocalClient() }
    val remoteBackendService: RemoteBackendService by lazy { RemoteBackendServiceImpl() }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val unlockChannel = NotificationChannel(
                CHANNEL_ID_UNLOCK,
                "KIDLOCK Unlock Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for unlock requests and temporary codes from child devices"
                enableVibration(true)
            }

            val securityChannel = NotificationChannel(
                CHANNEL_ID_SECURITY,
                "KIDLOCK Device Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pairing and connection status alerts"
            }

            notificationManager.createNotificationChannel(unlockChannel)
            notificationManager.createNotificationChannel(securityChannel)
        }
    }
}
