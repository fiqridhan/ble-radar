package com.example.bleradar.storage

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.bleradar.model.DeviceReading
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "device_history")
data class DeviceHistory(
    @PrimaryKey val address: String,
    val name: String?,
    val lastRssi: Int,
    val lastSeen: Long,
) {
    companion object {
        fun from(reading: DeviceReading, previousName: String?) = DeviceHistory(
            address = reading.address,
            name = reading.name ?: previousName,
            lastRssi = reading.rssi,
            lastSeen = reading.lastSeen,
        )
    }
}

@Dao
interface DeviceHistoryDao {
    @Query("SELECT * FROM device_history ORDER BY lastSeen DESC")
    fun observeAll(): Flow<List<DeviceHistory>>

    @Query("SELECT name FROM device_history WHERE address = :address LIMIT 1")
    suspend fun nameFor(address: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceHistory)
}

@Database(entities = [DeviceHistory::class], version = 1, exportSchema = false)
abstract class BleRadarDatabase : RoomDatabase() {
    abstract fun deviceHistoryDao(): DeviceHistoryDao
}
