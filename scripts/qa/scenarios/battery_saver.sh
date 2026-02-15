#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
set_battery_saver 1
output="$(run_inst_test com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest || true)"
set_battery_saver 0
status="$(result_from_output "$output")"
root="ok"
notes="battery saver"
if [[ "$status" != "PASS" ]]; then root="oem_background_restriction"; notes="$(tail -n 8 <<<"$output" | tr '\n' ' ')"; fi
if device_disconnected "$output"; then root="adb_device_unavailable"; fi
if instrumentation_missing "$output"; then root="instrumentation_not_installed"; fi
printf 'SCENARIO=battery_saver\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
