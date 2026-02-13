package com.spazoodle.guardian.platform.scheduler

import com.spazoodle.guardian.domain.model.Alarm

interface AlarmScheduler {
    fun scheduleMainTrigger(alarm: Alarm)
    fun cancelMainTrigger(alarmId: Long)
}
