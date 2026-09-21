package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY lastActivityTime DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE isPaired = 1 LIMIT 1")
    fun getPrimaryPairedDevice(): Flow<DeviceEntity?>

    @Query("SELECT * FROM devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(device: DeviceEntity)

    @Update
    suspend fun update(device: DeviceEntity)

    @Query("UPDATE devices SET lockState = :lockState WHERE deviceId = :deviceId")
    suspend fun updateLockState(deviceId: String, lockState: String)

    @Query("UPDATE devices SET batteryPct = :batteryPct, lastActivityTime = :time WHERE deviceId = :deviceId")
    suspend fun updateBatteryAndActivity(deviceId: String, batteryPct: Int, time: Long)

    @Query("UPDATE devices SET connectionState = :state WHERE deviceId = :deviceId")
    suspend fun updateConnectionState(deviceId: String, state: String)

    @Query("UPDATE devices SET name = :newName WHERE deviceId = :deviceId")
    suspend fun renameDevice(deviceId: String, newName: String)

    @Query("DELETE FROM devices WHERE deviceId = :deviceId")
    suspend fun deleteDevice(deviceId: String)

    @Query("DELETE FROM devices")
    suspend fun clearAll()
}
