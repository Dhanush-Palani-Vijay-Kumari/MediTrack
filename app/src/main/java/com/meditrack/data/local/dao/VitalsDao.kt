package com.meditrack.data.local.dao

import androidx.room.*
import com.meditrack.data.local.entity.VitalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalsDao {

    @Query("SELECT * FROM vitals ORDER BY timestamp DESC")
    fun getAllVitals(): Flow<List<VitalsEntity>>

    @Query("SELECT * FROM vitals ORDER BY timestamp DESC LIMIT 1")
    fun getLatestVitals(): Flow<VitalsEntity?>

    @Query("SELECT * FROM vitals WHERE timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    fun getVitalsSince(fromTimestamp: Long): Flow<List<VitalsEntity>>

    @Query("SELECT * FROM vitals WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun getVitalsForDate(dateKey: String): Flow<List<VitalsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitals(vitals: VitalsEntity): Long

    @Update
    suspend fun updateVitals(vitals: VitalsEntity)

    @Delete
    suspend fun deleteVitals(vitals: VitalsEntity)
}
