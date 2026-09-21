package com.example.data.repository

import com.example.data.local.dao.UnlockDao
import com.example.data.local.entity.TemporaryCodeEntity
import com.example.data.local.entity.UnlockRequestEntity
import com.example.model.RequestStatus
import com.example.model.TemporaryUnlockCode
import com.example.model.UnlockRequest
import com.example.security.CryptoUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UnlockRepository(private val unlockDao: UnlockDao) {

    val pendingRequestsFlow: Flow<List<UnlockRequest>> = unlockDao.getPendingRequests().map { list ->
        list.map { it.toModel() }
    }

    val allRequestsFlow: Flow<List<UnlockRequest>> = unlockDao.getAllRequests().map { list ->
        list.map { it.toModel() }
    }

    suspend fun createUnlockRequest(childDeviceId: String, childDeviceName: String): UnlockRequest {
        val requestId = "req_" + CryptoUtils.generateSecureToken(8)
        val entity = UnlockRequestEntity(
            requestId = requestId,
            childDeviceId = childDeviceId,
            childDeviceName = childDeviceName,
            requestedTimestamp = System.currentTimeMillis(),
            status = RequestStatus.PENDING.name,
            responseCode = null
        )
        unlockDao.insertRequest(entity)
        return entity.toModel()
    }

    suspend fun approveRequest(requestId: String, responseCode: String? = null) {
        unlockDao.updateRequestStatus(requestId, RequestStatus.APPROVED.name, responseCode)
    }

    suspend fun denyRequest(requestId: String) {
        unlockDao.updateRequestStatus(requestId, RequestStatus.DENIED.name, null)
    }

    suspend fun generateTemporaryCode(childDeviceId: String): TemporaryUnlockCode {
        val code = CryptoUtils.generateSecure6DigitCode()
        val now = System.currentTimeMillis()
        val expiresAt = now + 120_000L // 2 minutes expiration
        val entity = TemporaryCodeEntity(
            code = code,
            childDeviceId = childDeviceId,
            createdAt = now,
            expiresAt = expiresAt,
            isUsed = false
        )
        unlockDao.insertTemporaryCode(entity)
        return TemporaryUnlockCode(
            code = code,
            childDeviceId = childDeviceId,
            createdAt = now,
            expiresAt = expiresAt,
            isUsed = false
        )
    }

    suspend fun verifyAndConsumeCode(code: String): Boolean {
        val entity = unlockDao.getValidCode(code) ?: return false
        if (System.currentTimeMillis() > entity.expiresAt) {
            return false
        }
        unlockDao.markCodeAsUsed(code)
        return true
    }

    fun getActiveCodeForChild(childDeviceId: String): Flow<TemporaryUnlockCode?> {
        val now = System.currentTimeMillis()
        return unlockDao.getActiveCodeForChild(childDeviceId, now).map { entity ->
            entity?.let {
                TemporaryUnlockCode(
                    code = it.code,
                    childDeviceId = it.childDeviceId,
                    createdAt = it.createdAt,
                    expiresAt = it.expiresAt,
                    isUsed = it.isUsed
                )
            }
        }
    }

    private fun UnlockRequestEntity.toModel(): UnlockRequest {
        return UnlockRequest(
            requestId = requestId,
            childDeviceId = childDeviceId,
            childDeviceName = childDeviceName,
            requestedTimestamp = requestedTimestamp,
            status = try { RequestStatus.valueOf(status) } catch (_: Exception) { RequestStatus.PENDING },
            responseCode = responseCode
        )
    }
}
