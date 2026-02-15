#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
adb shell cmd notification set_dnd none >/dev/null 2>&1 || true
output="$(run_inst_test com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest || true)"
set_dnd_off
status="$(result_from_output "$output")"
root="ok"
notes="dnd none"
if [[ "$status" != "PASS" ]]; then root="dnd_policy_or_delivery_failure"; notes="$(tail -n 8 <<<"$output" | tr '\n' ' ')"; fi
if device_disconnected "$output"; then root="adb_device_unavailable"; fi
if instrumentation_missing "$output"; then root="instrumentation_not_installed"; fi
printf 'SCENARIO=dnd\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
