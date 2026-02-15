package com.spazoodle.guardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spazoodle.guardian.worker.GuardianRescheduleWorker

class PackageReplacedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        Log.i(TAG, "Package replaced event received. Enqueueing reschedule worker.")
        GuardianRescheduleWorker.enqueue(context.applicationContext, reason = "package_replaced")
    }

    companion object {
        private const val TAG = "PackageReplacedReceiver"
    }
}
