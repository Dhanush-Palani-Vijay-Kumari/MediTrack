package com.meditrack.data.repository

import com.meditrack.data.local.dao.AppointmentDao
import com.meditrack.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val dao: AppointmentDao
) {
    fun getAllAppointments(): Flow<List<AppointmentEntity>> = dao.getAllAppointments()

    fun getUpcomingAppointments(): Flow<List<AppointmentEntity>> =
        dao.getUpcomingAppointments(System.currentTimeMillis())

    fun getNextAppointment(): Flow<AppointmentEntity?> =
        dao.getNextAppointment(System.currentTimeMillis())

    fun getAppointmentById(id: Long): Flow<AppointmentEntity?> = dao.getAppointmentById(id)

    suspend fun insertAppointment(appointment: AppointmentEntity): Long =
        dao.insertAppointment(appointment)

    suspend fun updateAppointment(appointment: AppointmentEntity) =
        dao.updateAppointment(appointment)

    suspend fun deleteAppointment(appointment: AppointmentEntity) =
        dao.deleteAppointment(appointment)

    fun daysUntil(dateTimeMillis: Long): Int {
        val now = System.currentTimeMillis()
        val diff = dateTimeMillis - now
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
}
