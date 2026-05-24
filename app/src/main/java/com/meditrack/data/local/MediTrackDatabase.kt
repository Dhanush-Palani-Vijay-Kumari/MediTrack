package com.meditrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meditrack.data.local.dao.AppointmentDao
import com.meditrack.data.local.dao.MedicationDao
import com.meditrack.data.local.dao.VitalsDao
import com.meditrack.data.local.entity.AppointmentEntity
import com.meditrack.data.local.entity.MedicationEntity
import com.meditrack.data.local.entity.MedicationLogEntity
import com.meditrack.data.local.entity.VitalsEntity

@Database(
    entities = [
        MedicationEntity::class,
        MedicationLogEntity::class,
        VitalsEntity::class,
        AppointmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MediTrackDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun vitalsDao(): VitalsDao
    abstract fun appointmentDao(): AppointmentDao
}
