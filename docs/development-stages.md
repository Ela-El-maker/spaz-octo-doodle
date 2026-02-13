# guardian development stages

This is the execution roadmap from stage `0` to rollout. Each stage has scope, implementation checklist, and exit criteria.

## stage 0: product contract and scope lock

### implement

- lock MVP scope:
  - exact date-time alarms
  - pre-alerts
  - full-screen ringing
  - snooze
  - reboot/time-change rescheduling
  - reliability dashboard
  - event history
- define non-goals for v1 (for example: cloud sync, NLP input, geofencing)
- define reliability contract:
  - far-future scheduling
  - receiver/service chain integrity
  - diagnostics traceability

### exit criteria

- approved product scope document
- approved feature flags list for post-v1 features

## stage 1: repository and project setup

### implement

- move to Android app structure with Kotlin + Compose + Room
- setup modules/packages reflecting `docs/architecture.md`
- baseline tooling:
  - lint/format
  - unit test task
  - CI build and test workflow
- docs-first structure:
  - `docs/overview.md`
  - `docs/architecture.md`
  - this stage guide

### exit criteria

- CI green on build and tests
- project boots with placeholder screens and navigation

## stage 2: domain model and policy engine

### implement

- domain models:
  - `alarm`
  - `policy`
  - `trigger`
  - `alarm-instance-history`
- use cases:
  - create/update/enable/disable alarm
  - compute schedule plan
  - acknowledge/dismiss/snooze
- timezone strategy decisions and tests

### exit criteria

- unit tests for schedule generation pass
- trigger plan deterministic for same inputs

## stage 3: persistence layer

### implement

- Room entities and daos
- repository implementations
- migration strategy baseline
- queries for:
  - enabled alarms
  - upcoming alarms
  - history timeline

### exit criteria

- integration tests for CRUD and critical queries pass
- versioned schema committed

## stage 4: exact scheduling engine

### implement

- `alarm-scheduler` interface
- AlarmManager implementation with:
  - unique request codes per trigger
  - exact scheduling with idle allowance
  - cancellation and rescheduling paths
- scheduler diagnostics logs

### exit criteria

- create/cancel/edit flows correctly change scheduled intents
- manual test: alarm 2 minutes ahead fires at expected time

## stage 5: receivers and rescheduling lifecycle

### implement

- alarm trigger receiver
- boot completed receiver
- timezone/time-change receiver
- package replaced receiver (if used)
- reschedule-all use case wiring

### exit criteria

- reboot and timezone tests pass on device/emulator
- enabled alarms are restored after lifecycle events

## stage 6: ringing service and action loop

### implement

- foreground service for ringing
- full-screen notification and ringing activity
- actions:
  - stop
  - snooze
  - do (optional primary action)
- audio/vibration lifecycle cleanup and wake-lock scoping

### exit criteria

- lockscreen flow works
- stop/snooze/do are idempotent and logged

## stage 7: user-facing core screens

### implement

- alarm list with status badges and toggle
- create/edit screen with primary action fields and policy options
- ringing screen for active alarm
- input validation and state error handling

### exit criteria

- full user journey works for future-plan scenario
- no direct platform calls in UI layer

## stage 8: reliability dashboard

### implement

- device health checks:
  - notification permission
  - exact alarm status
  - battery optimization
  - full-screen readiness
  - dnd alarm behavior guidance
- one-tap deep links to relevant system settings
- test alarm flow and health score

### exit criteria

- dashboard surfaces high-risk states reliably
- test alarm path validates end-to-end chain

## stage 9: history, diagnostics, and proof

### implement

- history timeline screen
- drift and outcome recording
- diagnostics export/copy for issue reports
- support-facing event codes

### exit criteria

- failed/risky scenarios can be diagnosed from in-app history
- QA can attach diagnostics to bug reports

## stage 10: advanced features pack

### implement

- primary-action launch polish
- configurable nag mode
- escalation policies
- templates and defaults bundles

### exit criteria

- each advanced feature has:
  - unit tests
  - manual reliability run on target device matrix

## stage 11: qa hardening

### implement

- test matrix across OEM types
- overnight idle tests
- long-horizon scheduling tests
- release build smoke tests

### exit criteria

- reliability pass threshold met on matrix
- open defects triaged with severity and owners

## stage 12: compliance and rollout readiness

### implement

- store listing and permission rationale copy
- privacy policy alignment
- crash and ANR monitoring setup
- rollout gating checklist

### exit criteria

- release candidate approved
- known risks documented with mitigations

## stage 13: staged rollout and post-launch

### implement

- phased rollout: internal -> closed beta -> percentage production
- incident response runbook execution readiness
- early telemetry review cadence

### exit criteria

- rollout reaches stable production threshold
- post-launch backlog prioritized by reliability impact
