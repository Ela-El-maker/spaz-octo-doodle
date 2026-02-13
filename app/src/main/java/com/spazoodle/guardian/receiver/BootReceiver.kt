package com.spazoodle.guardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Boot event received. Reschedule flow will be wired in Stage 5.")
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
