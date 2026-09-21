package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AppConfigDao
import com.example.data.local.dao.DeviceDao
import com.example.data.local.dao.UnlockDao
import com.example.data.local.entity.AppConfigEntity
import com.example.data.local.entity.DeviceEntity
import com.example.data.local.entity.TemporaryCodeEntity
import com.example.data.local.entity.UnlockRequestEntity

@Database(
    entities = [
        DeviceEntity::class,
        UnlockRequestEntity::class,
        TemporaryCodeEntity::class,
        AppConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KidLockDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun unlockDao(): UnlockDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile
        private var INSTANCE: KidLockDatabase? = null

        fun getDatabase(context: Context): KidLockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KidLockDatabase::class.java,
                    "kidlock_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
