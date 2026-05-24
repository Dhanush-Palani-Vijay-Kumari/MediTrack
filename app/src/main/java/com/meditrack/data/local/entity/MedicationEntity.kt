package com.meditrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dosage: String,
    val form: String,           // tablet, capsule, liquid, injection
    val condition: String,      // what it treats
    val frequencyType: String,  // ONCE_DAILY, TWICE_DAILY, THREE_TIMES, WEEKLY, CUSTOM
    val morningTime: String?,   // "08:00"
    val afternoonTime: String?, // "13:00"
    val eveningTime: String?,   // "20:00"
    val instructions: String,   // "With breakfast", "After meal", etc.
    val startDate: Long,        // epoch millis
    val totalPills: Int,
    val pillsRemaining: Int,
    val isActive: Boolean = true,
    val remindersEnabled: Boolean = true,
    val color: String = "BLUE"  // BLUE, GREEN, AMBER, PURPLE, CORAL
)
