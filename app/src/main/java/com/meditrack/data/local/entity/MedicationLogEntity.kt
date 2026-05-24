package com.meditrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medication_logs",
    foreignKeys = [ForeignKey(
        entity = MedicationEntity::class,
        parentColumns = ["id"],
        childColumns = ["medicationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("medicationId")]
)
data class MedicationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val scheduledTime: Long,  // epoch millis of when it was due
    val takenTime: Long?,     // epoch millis of when it was taken, null = missed
    val status: String,       // TAKEN, MISSED, PENDING, SKIPPED
    val dateKey: String       // "2026-05-23" for easy daily grouping
)
