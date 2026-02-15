# Guardian

Guardian is a reliability-first Android alarm system for far-future, exact date-time plans and reminders.

## documentation

- `docs/overview.md`
- `docs/architecture.md`
- `docs/development-stages.md`
- `docs/project-setup.md`
- `docs/user-flow.md`
- `docs/user-flow.json`
- `docs/invariants.md`
- `docs/rollout-plan.md`
- `docs/qa/device-matrix.md`
- `docs/qa/test-scenarios.md`
- `docs/qa/overnight-and-long-horizon.md`
- `docs/qa/release-smoke.md`
- `docs/qa/defect-triage.md`
- `docs/compliance/permissions-rationale.md`
- `docs/compliance/privacy-policy-draft.md`
- `docs/compliance/store-release-readiness.md`
- `docs/ops/monitoring-checklist.md`
- `docs/ops/staged-rollout-playbook.md`
- `docs/ops/incident-response-runbook.md`
- `docs/ops/telemetry-review-cadence.md`
- `docs/decisions/adr-0001-architecture-and-reliability-first.md`
- `docs/runbooks/reliability-setup-and-verification.md`

## build

Run `./gradlew :app:assembleDebug`.

## run

Install and launch from Android Studio, or use:

- `./gradlew :app:installDebug`

./gradlew :app:assembleDebug :app:lintDebug --console=plain
./gradlew :app:installDebug --console=plain && adb shell pm list packages | grep -i -E "guardian|spazoodle