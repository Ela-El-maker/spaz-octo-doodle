package com.spazoodle.guardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spazoodle.guardian.worker.GuardianRescheduleWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action.orEmpty()
        val isBoot = action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        val isUnlock = action == Intent.ACTION_USER_UNLOCKED
        if (!isBoot && !isUnlock) {
            return
        }
        Log.i(TAG, "Boot event received ($action). Enqueueing reschedule worker.")
        val reason = if (isUnlock) "user_unlocked" else "boot"
        GuardianRescheduleWorker.enqueue(context.applicationContext, reason = reason)
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
