package com.meditrack.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meditrack.data.repository.MedicationRepository
import com.meditrack.util.ReminderScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RescheduleRemindersWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MedicationRepository,
    private val scheduler: ReminderScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val medications = repository.getActiveMedications().first()
        medications.forEach { med ->
            if (med.remindersEnabled) {
                scheduler.scheduleMedicationReminders(med)
            }
        }
        return Result.success()
    }
}
