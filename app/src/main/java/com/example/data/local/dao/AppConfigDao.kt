package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AppConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT configValue FROM app_config WHERE configKey = :key LIMIT 1")
    fun getConfigFlow(key: String): Flow<String?>

    @Query("SELECT configValue FROM app_config WHERE configKey = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfig(config: AppConfigEntity)

    @Query("DELETE FROM app_config WHERE configKey = :key")
    suspend fun removeConfig(key: String)

    @Query("DELETE FROM app_config")
    suspend fun clearAll()
}
