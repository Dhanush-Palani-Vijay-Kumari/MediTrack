package com.meditrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vitals")
data class VitalsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,        // epoch millis
    val dateKey: String,        // "2026-05-23"
    val systolic: Int?,         // blood pressure systolic mmHg
    val diastolic: Int?,        // blood pressure diastolic mmHg
    val heartRate: Int?,        // bpm
    val bloodSugar: Float?,     // mg/dL
    val weight: Float?,         // kg
    val oxygenSaturation: Int?, // SpO2 %
    val notes: String = ""
)
