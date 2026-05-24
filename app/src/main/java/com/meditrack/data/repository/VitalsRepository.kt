package com.meditrack.data.repository

import com.meditrack.data.local.dao.VitalsDao
import com.meditrack.data.local.entity.VitalsEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VitalsRepository @Inject constructor(
    private val dao: VitalsDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getLatestVitals(): Flow<VitalsEntity?> = dao.getLatestVitals()

    fun getAllVitals(): Flow<List<VitalsEntity>> = dao.getAllVitals()

    fun getVitalsSince(daysAgo: Int): Flow<List<VitalsEntity>> {
        val from = Instant.now().minusSeconds(daysAgo.toLong() * 86400)
        return dao.getVitalsSince(from.toEpochMilli())
    }

    suspend fun insertVitals(vitals: VitalsEntity): Long {
        val now = System.currentTimeMillis()
        val dateKey = Instant.ofEpochMilli(now)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(dateFormatter)
        return dao.insertVitals(vitals.copy(timestamp = now, dateKey = dateKey))
    }

    suspend fun deleteVitals(vitals: VitalsEntity) = dao.deleteVitals(vitals)

    fun getBpStatus(systolic: Int, diastolic: Int): VitalStatus = when {
        systolic < 120 && diastolic < 80 -> VitalStatus.NORMAL
        systolic < 130 && diastolic < 80 -> VitalStatus.ELEVATED
        systolic < 140 || diastolic < 90 -> VitalStatus.WATCH
        else -> VitalStatus.HIGH
    }

    fun getHeartRateStatus(bpm: Int): VitalStatus = when (bpm) {
        in 60..100 -> VitalStatus.NORMAL
        in 50..59, in 101..110 -> VitalStatus.WATCH
        else -> VitalStatus.HIGH
    }

    fun getBloodSugarStatus(mgDl: Float): VitalStatus = when {
        mgDl < 100 -> VitalStatus.NORMAL
        mgDl < 126 -> VitalStatus.WATCH
        else -> VitalStatus.HIGH
    }
}

enum class VitalStatus { NORMAL, ELEVATED, WATCH, HIGH }
