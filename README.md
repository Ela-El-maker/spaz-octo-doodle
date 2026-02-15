# Guardian

Guardian is a reliability-first Android alarm system for exact, date-time scheduling.
It is designed for plans that must not be silently dropped under normal Android lifecycle pressure.

## System Description

Guardian provides:

- One-time exact alarms for specific future date and time.
- Pre-alert triggers before main trigger.
- Ringing flow with stop/snooze and optional primary action.
- Reliability dashboard (permissions, exact-alarm capability, OEM risk signals).
- History and proof trail for fired, missed, and acknowledged events.
- Startup/boot/time-change rescheduling and missed-alarm reconciliation.

## Core Reliability Contract

- Enabled alarms are persisted in Room and scheduled via `AlarmManager`.
- Trigger reception is recorded before heavy runtime work.
- Ringing runs in a foreground service with notification/full-screen fallback path.
- One-time alarms are auto-finalized (completed or missed) to avoid stale active states.
- Dedupe/idempotency guards reduce duplicate side effects.

## Known Platform Limits

These are Android platform constraints, not app bugs:

- Force stop from system settings blocks alarm delivery until app is opened again.
- Clearing app data removes schedules.
- Device powered off cannot ring at trigger time (recovery applies after boot).

## Project Structure

- `app/`: Android app (UI, receivers, service, Room, platform integrations).
- `core-domain/`: domain models, repository contracts, use cases, scheduling rules.
- `docs/`: project documentation.

## Build and Install

- Build debug APK: `./gradlew :app:assembleDebug`
- Install on connected device: `./gradlew :app:installDebug`

## QA and Test Commands

- Unit/domain tests: `./gradlew :core-domain:test`
- Instrumented tests (connected device): `./gradlew :app:connectedDebugAndroidTest`
- E2E matrix harness: `scripts/qa/run_e2e_matrix.sh --connected-gate`

Reports are written to:

- `docs/qa/reports/latest-e2e.json`
- `docs/qa/reports/latest-e2e.md`

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details on:

- Development setup and coding standards
- How to report issues and submit pull requests
- Testing requirements and guidelines
- Code of conduct

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Documentation Index

- `docs/README.md`
- `docs/overview.md`
- `docs/architecture.md`
- `docs/testing.md`

Operational, compliance, rollout, and stage checklists are under `docs/`.
