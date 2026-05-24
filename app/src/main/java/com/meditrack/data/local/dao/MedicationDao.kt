package com.meditrack.data.local.dao

import androidx.room.*
import com.meditrack.data.local.entity.MedicationEntity
import com.meditrack.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    // ── Medications ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: Long): Flow<MedicationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)

    @Query("UPDATE medications SET pillsRemaining = pillsRemaining - 1 WHERE id = :id AND pillsRemaining > 0")
    suspend fun decrementPills(id: Long)

    // ── Medication Logs ───────────────────────────────────────────────────────

    @Query("SELECT * FROM medication_logs WHERE dateKey = :dateKey ORDER BY scheduledTime ASC")
    fun getLogsForDate(dateKey: String): Flow<List<MedicationLogEntity>>

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId ORDER BY scheduledTime DESC LIMIT 30")
    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLogEntity>>

    @Query("""
        SELECT dateKey, 
               COUNT(*) as total,
               SUM(CASE WHEN status = 'TAKEN' THEN 1 ELSE 0 END) as taken
        FROM medication_logs 
        WHERE medicationId = :medicationId 
        AND dateKey >= :fromDate
        GROUP BY dateKey
        ORDER BY dateKey ASC
    """)
    fun getAdherenceForMedication(medicationId: Long, fromDate: String): Flow<List<AdherenceRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLogEntity): Long

    @Update
    suspend fun updateLog(log: MedicationLogEntity)

    @Query("SELECT COUNT(*) FROM medication_logs WHERE dateKey = :dateKey AND status = 'TAKEN'")
    fun getTakenCountForDate(dateKey: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM medication_logs WHERE dateKey = :dateKey")
    fun getTotalCountForDate(dateKey: String): Flow<Int>

    // ── Streak ────────────────────────────────────────────────────────────────

    @Query("""
        SELECT DISTINCT dateKey FROM medication_logs 
        WHERE status = 'TAKEN' 
        ORDER BY dateKey DESC
    """)
    suspend fun getDistinctTakenDates(): List<String>
}

data class AdherenceRow(
    val dateKey: String,
    val total: Int,
    val taken: Int
)
