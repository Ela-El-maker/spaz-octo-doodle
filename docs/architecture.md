# guardian architecture

## major components

- `ui/`
  - Compose screens, view models, navigation, state holders
- `domain/`
  - use cases, scheduling policy rules, trigger planning, invariants
- `data/`
  - Room entities, daos, repository implementations, mappers
- `platform/`
  - AlarmManager scheduler, notification channels, permission/state probes
- `receiver/`
  - alarm, boot, time-change, and action receivers
- `service/`
  - foreground ringing service, audio/vibration lifecycle, wake-lock scope

## control flow

1. user creates alarm in Compose form
2. view model sends request to `CreateAlarmUseCase`
3. domain validates and computes trigger schedule plan
4. repository persists alarm and policy to Room
5. platform scheduler registers exact alarms with AlarmManager
6. Android fires broadcast at trigger time
7. receiver logs event and starts foreground ringing service
8. service shows full-screen alarm UI and notification actions
9. stop/snooze/do action flows back through use cases and history writes

## boundaries and assumptions

- UI never schedules directly with AlarmManager
- domain owns scheduling policy and trigger generation rules
- platform owns Android API behavior and permission checks
- every fired trigger is recorded in history for diagnostics
- exact alarm capability, notification delivery, and battery policy are treated as runtime health dependencies

## trigger model

All time-based behavior is represented as triggers:

- `main`
- `pre-alert`
- `snooze`
- `nag`

This single abstraction avoids rewrites when features expand.
