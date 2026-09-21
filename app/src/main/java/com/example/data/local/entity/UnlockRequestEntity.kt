package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlock_requests")
data class UnlockRequestEntity(
    @PrimaryKey val requestId: String,
    val childDeviceId: String,
    val childDeviceName: String,
    val requestedTimestamp: Long,
    val status: String, // PENDING, APPROVED, DENIED, EXPIRED
    val responseCode: String?
)

@Entity(tableName = "temporary_codes")
data class TemporaryCodeEntity(
    @PrimaryKey val code: String,
    val childDeviceId: String,
    val createdAt: Long,
    val expiresAt: Long,
    val isUsed: Boolean
)
