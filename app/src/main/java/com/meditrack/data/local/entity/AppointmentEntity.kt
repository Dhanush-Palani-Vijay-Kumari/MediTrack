package com.meditrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val doctorName: String,
    val specialty: String,
    val location: String,
    val dateTime: Long,       // epoch millis
    val notes: String = "",
    val reminderSent: Boolean = false,
    val status: String = "UPCOMING"  // UPCOMING, COMPLETED, CANCELLED
)
