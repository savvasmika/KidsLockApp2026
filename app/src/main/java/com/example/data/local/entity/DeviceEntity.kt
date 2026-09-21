package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val deviceId: String,
    val name: String,
    val deviceType: String,
    val ipAddress: String,
    val port: Int,
    val isPaired: Boolean,
    val sharedSecret: String,
    val lockState: String, // "LOCKED" or "UNLOCKED"
    val batteryPct: Int,
    val lastActivityTime: Long,
    val activeThemeId: String,
    val activeLanguage: String,
    val connectionState: String
)
