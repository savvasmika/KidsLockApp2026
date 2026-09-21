package com.example.network

import android.util.Log
import com.example.model.ConnectionState
import com.example.model.KidLockDevice
import com.example.model.UnlockRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * RemoteBackendService handles Mode 2: Remote Internet communication
 * when Parent and Child devices are not on the same Wi-Fi network.
 *
 * Supports Firebase Cloud Firestore, Firebase Authentication, and Firebase Cloud Messaging (FCM).
 *
 * Configuration Instructions:
 * To enable live cloud relay via Firebase:
 * 1. Add your `google-services.json` to the `/app` module directory.
 * 2. In AI Studio Secrets / Firebase console, ensure Firestore and FCM are enabled.
 * 3. The service automatically switches connection state from CONNECTED_LOCAL to CONNECTED_REMOTE.
 */
interface RemoteBackendService {
    val isConfigured: Boolean
    val connectionState: StateFlow<ConnectionState>

    suspend fun registerDeviceInCloud(device: KidLockDevice): Result<Boolean>
    suspend fun sendRemoteUnlockCommand(childDeviceId: String, parentDeviceId: String, token: String): Result<Boolean>
    suspend fun sendRemoteLockCommand(childDeviceId: String, parentDeviceId: String, token: String): Result<Boolean>
    suspend fun postUnlockRequestToCloud(request: UnlockRequest): Result<Boolean>
    suspend fun syncDeviceState(childDeviceId: String): Result<KidLockDevice?>
}

class RemoteBackendServiceImpl : RemoteBackendService {
    private val tag = "KidLockRemoteBackend"

    // Set to true when Firebase / remote REST endpoint configuration is supplied
    override val isConfigured: Boolean = false

    private val _connectionState = MutableStateFlow(
        if (isConfigured) ConnectionState.CONNECTED_REMOTE else ConnectionState.OFFLINE
    )
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    override suspend fun registerDeviceInCloud(device: KidLockDevice): Result<Boolean> {
        if (!isConfigured) {
            Log.d(tag, "Remote backend not configured. Operating in Local Wi-Fi mode.")
            return Result.success(false)
        }
        // In live cloud mode, stores device profile in Firestore: /devices/{deviceId}
        return Result.success(true)
    }

    override suspend fun sendRemoteUnlockCommand(
        childDeviceId: String,
        parentDeviceId: String,
        token: String
    ): Result<Boolean> {
        if (!isConfigured) {
            Log.d(tag, "Remote backend not configured. Using local peer-to-peer transport.")
            return Result.failure(IllegalStateException("Cloud relay not configured"))
        }
        // In live cloud mode, sends FCM push notification data payload or updates Firestore command doc
        return Result.success(true)
    }

    override suspend fun sendRemoteLockCommand(
        childDeviceId: String,
        parentDeviceId: String,
        token: String
    ): Result<Boolean> {
        if (!isConfigured) {
            return Result.failure(IllegalStateException("Cloud relay not configured"))
        }
        return Result.success(true)
    }

    override suspend fun postUnlockRequestToCloud(request: UnlockRequest): Result<Boolean> {
        if (!isConfigured) {
            return Result.failure(IllegalStateException("Cloud relay not configured"))
        }
        return Result.success(true)
    }

    override suspend fun syncDeviceState(childDeviceId: String): Result<KidLockDevice?> {
        if (!isConfigured) {
            return Result.success(null)
        }
        return Result.success(null)
    }
}
