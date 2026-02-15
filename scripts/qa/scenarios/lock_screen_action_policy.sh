#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
lock_screen
output="$(run_inst_test com.spazoodle.guardian.qa.ActionLaunchPolicyEvaluatorTest || true)"
unlock_screen
status="$(result_from_output "$output")"
root="ok"
notes="lock-screen unlock-required policy"
if [[ "$status" != "PASS" ]]; then root="lockscreen_policy_failure"; notes="$(tail -n 10 <<<"$output" | tr '\n' ' ')"; fi
if device_disconnected "$output"; then root="adb_device_unavailable"; fi
if instrumentation_missing "$output"; then root="instrumentation_not_installed"; fi
printf 'SCENARIO=lock_screen_action_policy\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
