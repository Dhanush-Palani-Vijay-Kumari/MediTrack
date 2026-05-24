package com.meditrack.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.meditrack.MainActivity
import com.meditrack.R
import com.meditrack.data.repository.MedicationRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class MedicationReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MedicationRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "medication_reminders"
        const val CHANNEL_NAME = "Medication Reminders"
        const val KEY_MED_ID = "med_id"
        const val KEY_MED_NAME = "med_name"
        const val KEY_DOSAGE = "med_dosage"
        const val KEY_INSTRUCTIONS = "instructions"

        fun schedule(context: Context, medId: Long, medName: String, dosage: String,
                     instructions: String, delayMinutes: Long, tag: String) {
            val data = workDataOf(
                KEY_MED_ID to medId,
                KEY_MED_NAME to medName,
                KEY_DOSAGE to dosage,
                KEY_INSTRUCTIONS to instructions
            )
            val request = OneTimeWorkRequestBuilder<MedicationReminderWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setInputData(data)
                .addTag(tag)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, request)
        }

        fun cancelReminder(context: Context, tag: String) {
            WorkManager.getInstance(context).cancelAllWorkByTag(tag)
        }
    }

    override suspend fun doWork(): Result {
        val medId = inputData.getLong(KEY_MED_ID, -1L)
        val medName = inputData.getString(KEY_MED_NAME) ?: "Medication"
        val dosage = inputData.getString(KEY_DOSAGE) ?: ""
        val instructions = inputData.getString(KEY_INSTRUCTIONS) ?: ""

        // Check med is still active before notifying
        val med = repository.getMedicationById(medId).first()
        if (med == null || !med.isActive || !med.remindersEnabled) return Result.success()

        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "medication_detail/$medId")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, medId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_pill)
            .setContentTitle("Time for $medName $dosage")
            .setContentText(instructions.ifEmpty { "Tap to mark as taken" })
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$instructions\n\nTap to open MediTrack and mark as taken."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(medId.toInt(), notification)

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders to take your medications on time"
            enableVibration(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
