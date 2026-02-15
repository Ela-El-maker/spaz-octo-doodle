# QA Test Scenarios

## Scope
These scenarios are required before beta/production promotion.

## Acceptance Bar
- `PASS`:
  - scenario succeeds as specified.
- `EXPECTED_LIMITATION`:
  - scenario fails due to Android/platform constraints and is explicitly allowed.
- `FAIL`:
  - any non-allowed failure. Release is blocked.

Non-impossible release gate:
- all required non-impossible scenarios must be `PASS`.
- only these may be `EXPECTED_LIMITATION`:
  - force-stop with pending alarm.
  - clear app data with pending alarm.
  - device powered off during trigger window (post-boot recovery still required).

## Automated Scenario Set
Use `scripts/qa/run_e2e_matrix.sh`:
1. baseline trigger pipeline.
2. DND behavior.
3. airplane mode.
4. battery saver.
5. doze force-idle.
6. lock screen behavior.
7. lock-screen action policy (unlock required).
8. stop guard (hold-to-confirm).
9. retention worker registration.
10. reboot reschedule.
11. diagnostics completeness.
12. multi-alarm + rapid create/delete stress.
13. force-stop (`EXPECTED_LIMITATION`).
14. data-clear (`EXPECTED_LIMITATION`).

## Manual-Required Evidence
Track in `docs/qa/reports/manual-state-scenarios.json`:
1. timezone change policy correctness.
2. manual time-change policy correctness.
3. device powered-off trigger-window recovery behavior.

## Root-Cause Tags
- `receiver_not_invoked`
- `service_not_started`
- `full_screen_suppressed_oem`
- `force_stop_platform_block`
- `data_clear_reset`
- `late_recovered`
- `missed_beyond_grace`
- `diagnostics_incomplete`
- `collision_or_dedupe_failure`
- `lockscreen_policy_failure`
- `stop_guard_failure`
- `retention_worker_not_registered`

## Required Artifacts Per Scenario
- status (`PASS|FAIL|EXPECTED_LIMITATION`).
- device + android version.
- root-cause tag.
- remediation text.
- diagnostics/logcat snapshot.
