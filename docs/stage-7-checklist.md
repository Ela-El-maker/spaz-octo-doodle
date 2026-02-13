# stage 7 checklist

## objective

Deliver core app screens (list/create/edit) wired to domain use cases, persistence, and scheduler.

## implemented

- [x] Added UI state models for home and editor screens:
  - `HomeUiState`
  - `EditorUiState`
  - `AlarmDraft`
- [x] Added `HomeViewModel` with app wiring for:
  - loading all alarms
  - loading create/edit drafts
  - save create/update via domain use cases
  - scheduling/canceling via scheduler
  - toggle enable/disable flow
- [x] Added list screen (`HomeScreen`):
  - displays alarms
  - enable/disable switch
  - navigation to create/edit
- [x] Added editor screen (`AlarmEditorScreen`):
  - title, date, time, optional primary action
  - pre-alert toggles
  - nag mode toggle
  - save/cancel actions
- [x] Updated navigation graph (`GuardianRoot`) with routes:
  - `home`
  - `editor?alarmId={alarmId}`
- [x] Extended repository contract for UI list path:
  - added `getAllAlarms()` to domain repository
  - implemented in Room DAO and app repository implementation
- [x] Added lifecycle ViewModel dependencies in `app/build.gradle.kts`.

## verification

- Domain verification command:
  - `./gradlew :core-domain:test --console=plain`
- Result: `BUILD SUCCESSFUL`

## known follow-up

- App-level compile/runtime verification remains blocked by local SDK Build-Tools corruption in this environment.
- Stage 8 will implement reliability dashboard checks and fix-actions UI.
