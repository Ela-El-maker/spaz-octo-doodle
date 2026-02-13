package com.spazoodle.guardian.platform.scheduler

import com.spazoodle.guardian.domain.model.SchedulePlan

interface AlarmScheduler {
    fun canScheduleExactAlarms(): Boolean
    fun schedule(plan: SchedulePlan)
    fun cancelAlarm(alarmId: Long)
    fun rescheduleAll(plans: List<SchedulePlan>)
}
