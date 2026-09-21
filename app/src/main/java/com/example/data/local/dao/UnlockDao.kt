package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.TemporaryCodeEntity
import com.example.data.local.entity.UnlockRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {
    // Unlock Requests
    @Query("SELECT * FROM unlock_requests ORDER BY requestedTimestamp DESC")
    fun getAllRequests(): Flow<List<UnlockRequestEntity>>

    @Query("SELECT * FROM unlock_requests WHERE status = 'PENDING' ORDER BY requestedTimestamp DESC")
    fun getPendingRequests(): Flow<List<UnlockRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: UnlockRequestEntity)

    @Query("UPDATE unlock_requests SET status = :status, responseCode = :code WHERE requestId = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String, code: String? = null)

    @Query("DELETE FROM unlock_requests WHERE requestId = :requestId")
    suspend fun deleteRequest(requestId: String)

    // Temporary Codes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemporaryCode(code: TemporaryCodeEntity)

    @Query("SELECT * FROM temporary_codes WHERE code = :code AND isUsed = 0 LIMIT 1")
    suspend fun getValidCode(code: String): TemporaryCodeEntity?

    @Query("UPDATE temporary_codes SET isUsed = 1 WHERE code = :code")
    suspend fun markCodeAsUsed(code: String)

    @Query("SELECT * FROM temporary_codes WHERE childDeviceId = :childDeviceId AND isUsed = 0 AND expiresAt > :now ORDER BY createdAt DESC LIMIT 1")
    fun getActiveCodeForChild(childDeviceId: String, now: Long): Flow<TemporaryCodeEntity?>
}
