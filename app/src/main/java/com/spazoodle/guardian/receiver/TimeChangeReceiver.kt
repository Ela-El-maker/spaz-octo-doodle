package com.spazoodle.guardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spazoodle.guardian.worker.GuardianRescheduleWorker

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action.orEmpty()
        val relevant = action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED
        if (!relevant) return
        Log.i(TAG, "Time change event received ($action). Enqueueing reschedule worker.")
        GuardianRescheduleWorker.enqueue(context.applicationContext, reason = "time_change")
    }

    companion object {
        private const val TAG = "TimeChangeReceiver"
    }
}
