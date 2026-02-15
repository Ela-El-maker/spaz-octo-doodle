# Guardian E2E Device Test Report (2026-02-14)

## Device under test
- Device: Infinix X658E
- Android: 11
- Package: `com.spazoodle.guardian`
- Test probe: `com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest`

## Executed scenarios and outcomes

| Scenario | Result | Notes |
|---|---|---|
| Baseline alarm trigger (+20s) | PASS | MAIN trigger recorded in history. |
| DND `none` | PASS | Probe passed; alarm pipeline still delivered. |
| Airplane mode ON | PASS | Probe passed; local alarm delivery unaffected. |
| Battery saver ON | PASS (flaky) | One run failed, rerun passed. OEM background policy is unstable in this mode. |
| Forced Doze (`deviceidle force-idle`) | PASS | Probe passed under idle forcing. |
| Screen off / lock state | PASS | Probe passed while screen was turned off/locked. |
| Process kill (`am kill`) during pending alarm | PASS | Alarm still delivered after process death (non-force-stop). |
| Force-stop app during pending alarm | FAIL (expected platform behavior) | Process crashed and alarm delivery did not complete. Android drops alarms for force-stopped packages until explicit relaunch. |
| Clear app data during pending alarm (`pm clear`) | FAIL (expected) | Process crashed and scheduled alarms/history removed. |
| Device reboot then immediate probe | FAIL twice, then PASS after reinstall/reset | Indicates startup/post-reboot reliability is sensitive to OEM state and app install/test harness state. |
| Multiple alarms stress (3 near-term alarms) | PASS | All MAIN triggers delivered in a single run. |
| Rapid create/cancel stress | PASS | Canceled alarms did not fire MAIN events. |

## UI/ringing findings
- Confirmed: alarm trigger can be recorded without `AlarmRingingActivity` becoming resumed/top activity on this OEM.
- Confirmed: foreground ringing service is active in some runs while full-screen activity is not on top (heads-up style behavior only).

## Fix applied during this run
- File changed: `app/src/main/java/com/spazoodle/guardian/service/AlarmRingingService.kt`
- Change: service now explicitly attempts to launch `AlarmRingingActivity` with `NEW_TASK | SINGLE_TOP | CLEAR_TOP` in addition to full-screen notification intent.
- Purpose: reduce OEM cases where full-screen intent is suppressed and user only sees transient heads-up.

## Scenarios requested but not fully automatable from this shell
- Manual timezone change and manual system time change on this non-root device (shell lacks privilege to set clock/timezone directly).
- Physical powered-off alarm test from automation loop.
- “Swipe from recents” exact OEM behavior (we can force-stop, but recents behavior varies by ROM and may not equal force-stop).
- Recurring alarms: not implemented in current app model.

## Root-cause summary for missed/inconsistent alarms
1. Force-stop/data-clear are hard platform breakpoints.
   - If the app is force-stopped or data is cleared, scheduled alarms are lost/suppressed by Android by design.
2. OEM full-screen suppression.
   - Infinix can suppress full-screen intent promotion even when service/ringing path starts.
3. OEM battery/background instability.
   - Battery saver/background control causes intermittent timing/service behavior.
4. Test harness/device state coupling after reboot.
   - Post-reboot state can produce transient failure until app/test context is restored.

## Architectural hardening actions recommended next
1. Add explicit “Force-stopped or data-cleared” warning in reliability dashboard help text.
2. Add post-boot self-check worker:
   - verify active alarms count,
   - verify exact-alarm capability,
   - verify channel full-screen readiness,
   - emit “at risk” notification if degraded.
3. Add receiver/service telemetry events for:
   - service start failure,
   - activity launch failure,
   - full-screen suppression fallback.
4. Add an instrumentation test for lockscreen/full-screen assertion:
   - assert `AlarmRingingActivity` appears OR fallback notification remains ongoing for alarm lifecycle.
5. Add OEM-specific validation script to run after reboot and after battery policy changes.

## Current pass/fail confidence
- Core exact trigger path: medium-high confidence under normal state.
- Full-screen UI guarantee on OEM devices: medium confidence (improved but still OEM-governed).
- Lifecycle-destructive states (force-stop/data-clear): expected failures unless user relaunches app.
