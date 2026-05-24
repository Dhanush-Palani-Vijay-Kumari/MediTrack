package com.meditrack.util

import android.content.Context
import com.meditrack.data.local.entity.MedicationEntity
import com.meditrack.worker.MedicationReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun scheduleMedicationReminders(med: MedicationEntity) {
        if (!med.remindersEnabled) return

        val times = listOfNotNull(
            med.morningTime?.let { it to "morning" },
            med.afternoonTime?.let { it to "afternoon" },
            med.eveningTime?.let { it to "evening" }
        )

        times.forEach { (timeStr, period) ->
            val time = LocalTime.parse(timeStr, timeFmt)
            val now = LocalDateTime.now()
            var scheduled = now.withHour(time.hour).withMinute(time.minute).withSecond(0)
            if (scheduled.isBefore(now)) scheduled = scheduled.plusDays(1)

            val delayMinutes = ChronoUnit.MINUTES.between(now, scheduled)
            val tag = "med_${med.id}_${period}"

            MedicationReminderWorker.schedule(
                context = context,
                medId = med.id,
                medName = med.name,
                dosage = med.dosage,
                instructions = med.instructions,
                delayMinutes = delayMinutes,
                tag = tag
            )
        }
    }

    fun cancelMedicationReminders(medId: Long) {
        listOf("morning", "afternoon", "evening").forEach { period ->
            MedicationReminderWorker.cancelReminder(context, "med_${medId}_${period}")
        }
    }
}
