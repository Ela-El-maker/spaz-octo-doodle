# Guardian Architecture

## Layers

- `core-domain/`
  - Domain models, scheduling rules, and use cases.
  - Repository contracts independent of Android framework.
- `app/data/`
  - Room entities/DAO, repository implementations, mapping.
- `app/platform/`
  - AlarmManager scheduling, reliability scanners, OEM guidance helpers.
- `app/receiver/`
  - Trigger and system event entry points (`BOOT_COMPLETED`, time changes, package replace).
- `app/service/`
  - Foreground ringing service, audio/vibration, notifications.
- `app/ui/`
  - Compose screens and view models for home/editor/reliability/history.

## End-to-End Flow

1. User creates or edits an alarm in Compose UI.
2. ViewModel builds domain alarm model and persists through repository.
3. Scheduler registers OS-level trigger intents for the alarm plan.
4. At fire time, receiver validates and records event, then starts ringing service.
5. Ringing service handles audio/vibration and action routing (stop/snooze/action).
6. History is updated for proof, diagnostics, and UI state.

## Core Rules

- Trigger handling is idempotent and deduped.
- One-time alarms are finalized (disabled) after completion or missed classification.
- Startup reconciliation handles stale enabled alarms and reschedules valid ones.
- UI state is reactive from data source changes (no manual app restart needed).
