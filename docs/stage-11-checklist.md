# stage 11 checklist

## objective

Harden quality gates with an explicit QA matrix, repeatable scenarios, and defect triage workflow.

## implemented

- [x] Added QA device matrix (`docs/qa/device-matrix.md`).
- [x] Added required scenario suite (`docs/qa/test-scenarios.md`).
- [x] Added overnight and long-horizon protocol (`docs/qa/overnight-and-long-horizon.md`).
- [x] Added release smoke checklist (`docs/qa/release-smoke.md`).
- [x] Added defect triage model and SLA (`docs/qa/defect-triage.md`).
- [x] Updated stage guide wording to generalized action model (`do` vs `join`).
- [x] Updated reliability runbook action wording (`stop/snooze/do`).

## exit criteria mapping

- test matrix across OEM types: covered in `docs/qa/device-matrix.md`
- overnight idle tests: covered in `docs/qa/overnight-and-long-horizon.md`
- long-horizon scheduling tests: covered in `docs/qa/overnight-and-long-horizon.md`
- release build smoke tests: covered in `docs/qa/release-smoke.md`
- defect triage with severity/owner: covered in `docs/qa/defect-triage.md`

## verification

- Documentation integrity checks completed.
- Domain verification command:
  - `./gradlew :core-domain:test --console=plain`
- Result: `BUILD SUCCESSFUL`

## known follow-up

- App-level compile/runtime verification remains blocked by local SDK Build-Tools corruption in this environment.
- Stage 12 should finalize store/compliance docs, release gates, and monitoring checklists.
