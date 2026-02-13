package com.spazoodle.guardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Time change event received. Reschedule flow will be wired in Stage 5.")
    }

    companion object {
        private const val TAG = "TimeChangeReceiver"
    }
}
