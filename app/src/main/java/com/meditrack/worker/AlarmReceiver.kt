package com.meditrack.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule reminders after device restart
            val work = OneTimeWorkRequestBuilder<RescheduleRemindersWorker>().build()
            WorkManager.getInstance(context).enqueue(work)
        }
    }
}
