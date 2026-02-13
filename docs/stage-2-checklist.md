# stage 2 checklist

## objective

Lock domain contracts before Android-specific behavior expands.

## implemented

- [x] Added dedicated `core-domain` module for pure business logic.
- [x] Added domain models for:
  - `Alarm`, `AlarmType`
  - `AlarmPolicy`, `PreAlertOffset`, `NagSpec`, `EscalationSpec`
  - `SnoozeSpec`, `Trigger`, `TriggerKind`, `SchedulePlan`
  - `AlarmEvent`, `AlarmEventOutcome`
- [x] Added domain interfaces:
  - `Clock`
  - `AlarmRepository`
  - `AlarmHistoryRepository`
- [x] Added use cases:
  - `CreateAlarmUseCase`
  - `UpdateAlarmUseCase`
  - `EnableAlarmUseCase`
  - `DisableAlarmUseCase`
  - `ComputeSchedulePlanUseCase`
  - `RecordFireEventUseCase`
  - `AcknowledgeAlarmUseCase`
- [x] Added domain tests for:
  - schedule plan generation and ordering
  - pre-alert filtering when already past
  - create alarm future-time validation

## integration updates

- [x] `app` now depends on `:core-domain`.
- [x] Removed duplicate `app` domain source files.
- [x] Expanded Room entity and repository mapping to align with Stage 2 policy fields.
- [x] CI now runs `:core-domain:test`.

## verification

- Command: `./gradlew :core-domain:test --console=plain`
- Result: `BUILD SUCCESSFUL`

## known follow-up

- Android module build remains blocked by local SDK Build-Tools `34.0.0` corruption in this environment.
- Stage 3 will add history persistence entities/dao and migrations.
