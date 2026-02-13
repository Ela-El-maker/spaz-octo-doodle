# stage 4 checklist

## objective

Bind domain schedule plans to platform exact scheduling with stable trigger identity.

## implemented

- [x] Upgraded scheduler contract from single-main trigger to schedule-plan based API:
  - `canScheduleExactAlarms()`
  - `schedule(plan)`
  - `cancelAlarm(alarmId)`
  - `rescheduleAll(plans)`
- [x] Added stable request-code factory in domain:
  - `TriggerRequestCodeFactory`
- [x] Added trigger-code stability tests and kind differentiation tests.
- [x] Added reschedule-all domain use case:
  - `RescheduleAllActiveAlarmsUseCase`
- [x] Added scheduler registry in app to persist request codes per alarm for accurate cancellation.
- [x] `AndroidAlarmScheduler` now schedules all triggers in a `SchedulePlan` with exact alarms.
- [x] Added payload extras for trigger metadata:
  - alarm id
  - trigger kind
  - trigger index
  - trigger key
  - scheduled-at timestamp
- [x] Receiver now forwards trigger metadata to ringing service.

## verification

- Command: `./gradlew :core-domain:test --console=plain`
- Result: `BUILD SUCCESSFUL`

## known follow-up

- App-level scheduler instrumentation tests remain pending due local SDK Build-Tools corruption in this environment.
- Stage 5 will wire boot/time-change receivers to `RescheduleAllActiveAlarmsUseCase` and scheduler `rescheduleAll`.
