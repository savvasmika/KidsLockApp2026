package com.example.model

enum class RequestStatus {
    PENDING,
    APPROVED,
    DENIED,
    EXPIRED
}

data class UnlockRequest(
    val requestId: String,
    val childDeviceId: String,
    val childDeviceName: String,
    val requestedTimestamp: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING,
    val responseCode: String? = null
)

data class TemporaryUnlockCode(
    val code: String,
    val childDeviceId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 120_000L, // 2 minutes
    val isUsed: Boolean = false
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt
}
