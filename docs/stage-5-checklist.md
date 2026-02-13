# stage 5 checklist

## objective

Wire receiver lifecycle so triggers and system events are handled safely and alarms are rescheduled on platform changes.

## implemented

- [x] Added runtime wiring object to provide repositories, scheduler, and use cases from app context:
  - `GuardianRuntime`
- [x] Added app clock implementation:
  - `SystemUtcClock`
- [x] `AlarmTriggerReceiver` now:
  - runs asynchronously via `goAsync` + IO coroutine
  - validates alarm exists and is enabled
  - records fired event (`RecordFireEventUseCase`)
  - routes `PRE_ALERT` to notification path
  - routes `MAIN/SNOOZE/NAG` to foreground service path
- [x] `BootReceiver` now reschedules all enabled alarms using:
  - `RescheduleAllActiveAlarmsUseCase`
  - `AlarmScheduler.rescheduleAll`
- [x] `TimeChangeReceiver` now reschedules all enabled alarms on time/date/timezone changes.
- [x] Added `PackageReplacedReceiver` to reschedule after app update.
- [x] Manifest updated with `MY_PACKAGE_REPLACED` receiver registration.

## verification

- Domain verification command:
  - `./gradlew :core-domain:test --console=plain`
- Result: `BUILD SUCCESSFUL`

## known follow-up

- App compile/instrumentation verification remains blocked by local SDK Build-Tools corruption.
- Stage 6 will implement full ringing lifecycle (audio focus, stop/snooze/join actions, lockscreen behavior).
