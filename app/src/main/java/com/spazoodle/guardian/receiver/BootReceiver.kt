package com.spazoodle.guardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spazoodle.guardian.runtime.GuardianRuntime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Boot event received. Rescheduling enabled alarms.")
                val plans = GuardianRuntime
                    .rescheduleAllActiveAlarmsUseCase(appContext)
                    .invoke()
                GuardianRuntime.alarmScheduler(appContext).rescheduleAll(plans)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
