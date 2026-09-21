package com.example.data.repository

import com.example.data.local.dao.DeviceDao
import com.example.data.local.entity.DeviceEntity
import com.example.model.ConnectionState
import com.example.model.KidLockDevice
import com.example.model.LockState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepository(private val deviceDao: DeviceDao) {

    val allDevicesFlow: Flow<List<KidLockDevice>> = deviceDao.getAllDevices().map { list ->
        list.map { it.toModel() }
    }

    val primaryDeviceFlow: Flow<KidLockDevice?> = deviceDao.getPrimaryPairedDevice().map {
        it?.toModel()
    }

    suspend fun saveDevice(device: KidLockDevice) {
        deviceDao.insertOrUpdate(device.toEntity())
    }

    suspend fun getDevice(deviceId: String): KidLockDevice? {
        return deviceDao.getDeviceById(deviceId)?.toModel()
    }

    suspend fun updateLockState(deviceId: String, lockState: LockState) {
        deviceDao.updateLockState(deviceId, lockState.name)
    }

    suspend fun updateBatteryAndActivity(deviceId: String, batteryPct: Int) {
        deviceDao.updateBatteryAndActivity(deviceId, batteryPct, System.currentTimeMillis())
    }

    suspend fun updateConnectionState(deviceId: String, state: ConnectionState) {
        deviceDao.updateConnectionState(deviceId, state.name)
    }

    suspend fun renameDevice(deviceId: String, newName: String) {
        deviceDao.renameDevice(deviceId, newName)
    }

    suspend fun unpairDevice(deviceId: String) {
        deviceDao.deleteDevice(deviceId)
    }

    suspend fun clearAll() {
        deviceDao.clearAll()
    }

    private fun DeviceEntity.toModel(): KidLockDevice {
        return KidLockDevice(
            deviceId = deviceId,
            name = name,
            deviceType = deviceType,
            ipAddress = ipAddress,
            port = port,
            isPaired = isPaired,
            sharedSecret = sharedSecret,
            lockState = try { LockState.valueOf(lockState) } catch (_: Exception) { LockState.LOCKED },
            batteryPct = batteryPct,
            lastActivityTime = lastActivityTime,
            activeThemeId = activeThemeId,
            activeLanguage = activeLanguage,
            connectionState = try { ConnectionState.valueOf(connectionState) } catch (_: Exception) { ConnectionState.OFFLINE }
        )
    }

    private fun KidLockDevice.toEntity(): DeviceEntity {
        return DeviceEntity(
            deviceId = deviceId,
            name = name,
            deviceType = deviceType,
            ipAddress = ipAddress,
            port = port,
            isPaired = isPaired,
            sharedSecret = sharedSecret,
            lockState = lockState.name,
            batteryPct = batteryPct,
            lastActivityTime = lastActivityTime,
            activeThemeId = activeThemeId,
            activeLanguage = activeLanguage,
            connectionState = connectionState.name
        )
    }
}
