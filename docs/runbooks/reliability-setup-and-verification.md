# Reliability Setup and Verification Runbook

Use this before test runs, beta rollout, and support triage.

## Setup Checklist

1. notification permission granted
2. exact alarm capability available
3. battery optimization exemptions applied where needed
4. alarm notification channel configured correctly
5. full-screen alarm behavior verified on lockscreen

## Quick Verification Flow

1. schedule test alarm `+15s`
2. verify receiver is triggered
3. verify foreground service starts
4. verify full-screen ringing UI appears
5. verify Stop requires hold-to-confirm (not one-tap)
6. verify Do action shows unlock requirement when device is locked
7. verify stop, snooze, and do actions
8. verify history entries exist for the flow

## Automated Matrix Run

1. connect a device with USB debugging enabled.
2. run `scripts/qa/run_e2e_matrix.sh --connected-gate`.
3. review:
   - `docs/qa/reports/latest-e2e.json`
   - `docs/qa/reports/latest-e2e.md`
   - `docs/qa/reports/manual-state-scenarios.json`
4. collect run artifacts in `docs/qa/reports/artifacts`.

## Reboot Verification
1. schedule alarm for `+2m`
2. reboot device before trigger
3. confirm alarm still fires
4. confirm history includes reschedule event

## Timezone Verification

1. schedule alarm for near future
2. change timezone
3. verify trigger time reflects defined timezone policy
4. confirm reschedule event exists

## Expected Limitation Definitions
- `force_stop_platform_block`: Android force-stop blocks background delivery until user relaunches app.
- `data_clear_reset`: clearing app data removes alarms and reliability state.
- `powered_off_trigger_window`: alarm cannot ring while device is powered off; verify post-boot recovery behavior.

## Locked Policies
- lock-screen primary action: require device unlock before external launch.
- stop safeguard: hold-to-confirm on ringing UI for all alarms.
- retention cleanup: periodic 30-day prune via WorkManager plus startup fallback prune.
- audio route: speaker force is best effort; warning is surfaced if external route remains active.

## Issue Triage Minimum Data

- alarm id
- scheduled time
- fired time
- action outcome
- capability states at fire time
- device model and Android version
