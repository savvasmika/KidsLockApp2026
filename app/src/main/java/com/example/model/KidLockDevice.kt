package com.example.model

data class KidLockDevice(
    val deviceId: String,
    val name: String,
    val deviceType: String = "Tablet", // "Tablet" or "Phone"
    val ipAddress: String = "",
    val port: Int = 8765,
    val isPaired: Boolean = false,
    val sharedSecret: String = "",
    val lockState: LockState = LockState.LOCKED,
    val batteryPct: Int = -1,
    val lastActivityTime: Long = System.currentTimeMillis(),
    val activeThemeId: String = "space",
    val activeLanguage: String = "en",
    val connectionState: ConnectionState = ConnectionState.OFFLINE
)
