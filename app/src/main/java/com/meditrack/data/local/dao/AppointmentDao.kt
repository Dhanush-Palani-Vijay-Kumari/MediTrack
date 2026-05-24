package com.meditrack.data.local.dao

import androidx.room.*
import com.meditrack.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments ORDER BY dateTime ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE dateTime >= :now AND status = 'UPCOMING' ORDER BY dateTime ASC")
    fun getUpcomingAppointments(now: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE dateTime >= :now AND status = 'UPCOMING' ORDER BY dateTime ASC LIMIT 1")
    fun getNextAppointment(now: Long): Flow<AppointmentEntity?>

    @Query("SELECT * FROM appointments WHERE id = :id")
    fun getAppointmentById(id: Long): Flow<AppointmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)
}
