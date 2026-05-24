package com.meditrack.data.repository

import com.meditrack.data.local.dao.AdherenceRow
import com.meditrack.data.local.dao.MedicationDao
import com.meditrack.data.local.entity.MedicationEntity
import com.meditrack.data.local.entity.MedicationLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val dao: MedicationDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getActiveMedications(): Flow<List<MedicationEntity>> = dao.getActiveMedications()

    fun getAllMedications(): Flow<List<MedicationEntity>> = dao.getAllMedications()

    fun getMedicationById(id: Long): Flow<MedicationEntity?> = dao.getMedicationById(id)

    suspend fun insertMedication(medication: MedicationEntity): Long = dao.insertMedication(medication)

    suspend fun updateMedication(medication: MedicationEntity) = dao.updateMedication(medication)

    suspend fun deleteMedication(medication: MedicationEntity) = dao.deleteMedication(medication)

    fun getLogsForDate(date: LocalDate): Flow<List<MedicationLogEntity>> =
        dao.getLogsForDate(date.format(dateFormatter))

    fun getLogsForMedication(medicationId: Long): Flow<List<MedicationLogEntity>> =
        dao.getLogsForMedication(medicationId)

    fun getAdherenceForMedication(medicationId: Long, fromDate: LocalDate): Flow<List<AdherenceRow>> =
        dao.getAdherenceForMedication(medicationId, fromDate.format(dateFormatter))

    suspend fun insertLog(log: MedicationLogEntity): Long = dao.insertLog(log)

    suspend fun updateLog(log: MedicationLogEntity) = dao.updateLog(log)

    fun getTakenCountForDate(date: LocalDate): Flow<Int> =
        dao.getTakenCountForDate(date.format(dateFormatter))

    fun getTotalCountForDate(date: LocalDate): Flow<Int> =
        dao.getTotalCountForDate(date.format(dateFormatter))

    suspend fun markDoseTaken(log: MedicationLogEntity) {
        dao.updateLog(log.copy(status = "TAKEN", takenTime = System.currentTimeMillis()))
        dao.decrementPills(log.medicationId)
    }

    suspend fun calculateStreak(): Int {
        val dates = dao.getDistinctTakenDates()
        if (dates.isEmpty()) return 0
        var streak = 0
        var current = LocalDate.now()
        for (dateStr in dates) {
            val date = LocalDate.parse(dateStr, dateFormatter)
            if (date == current || date == current.minusDays(1)) {
                streak++
                current = date.minusDays(1)
            } else break
        }
        return streak
    }
}
