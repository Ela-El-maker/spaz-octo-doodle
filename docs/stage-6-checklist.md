# stage 6 checklist

## objective

Deliver alarm-grade ringing flow with foreground service, lockscreen/full-screen UI, and action handling.

## implemented

- [x] Added action receiver for alarm control actions:
  - `AlarmActionReceiver`
  - handles `STOP`, `SNOOZE`, `DO`
  - records acknowledgement outcomes
  - schedules exact snooze trigger
  - stops ringing service on action
- [x] Added full-screen ringing activity:
  - `AlarmRingingActivity`
  - lockscreen-safe (`showWhenLocked`, `turnScreenOn`)
  - buttons: `Stop`, `Snooze 5/10/15`, `Do`
- [x] Upgraded `AlarmRingingService` to real ring lifecycle:
  - starts foreground notification with full-screen intent
  - requests audio focus (alarm usage)
  - plays looping alarm tone via `MediaPlayer`
  - starts repeating vibration pattern
  - acquires/release short wake lock
  - exposes notification actions (`Stop`, `Snooze 10`, optional `Do`)
  - releases resources in `onDestroy`
- [x] `AlarmTriggerReceiver` now forwards alarm title and primary action to service.
- [x] Manifest updates:
  - receiver registration for `AlarmActionReceiver`
  - activity registration for `AlarmRingingActivity`
  - `VIBRATE` permission

## verification

- Domain verification command:
  - `./gradlew :core-domain:test --console=plain`
- Result: `BUILD SUCCESSFUL`

## known follow-up

- App compile/instrumentation remains blocked by local SDK Build-Tools corruption in this environment.
- Stage 7 will implement full create/edit/list screens wired to repositories and schedule planner.
