#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
lock_screen
output="$(run_inst_test com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest || true)"
unlock_screen
status="$(result_from_output "$output")"
root="ok"
notes="lock screen"
if [[ "$status" != "PASS" ]]; then root="full_screen_suppressed_oem"; notes="$(tail -n 8 <<<"$output" | tr '\n' ' ')"; fi
if device_disconnected "$output"; then root="adb_device_unavailable"; fi
if instrumentation_missing "$output"; then root="instrumentation_not_installed"; fi
printf 'SCENARIO=lock_screen\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
