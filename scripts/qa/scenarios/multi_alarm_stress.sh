#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
output="$(run_inst_test com.spazoodle.guardian.ui.MultiAlarmStressIntegrationTest || true)"
status="$(result_from_output "$output")"
root="ok"
notes="multi alarm + rapid create/delete"
if [[ "$status" != "PASS" ]]; then root="collision_or_dedupe_failure"; notes="$(tail -n 12 <<<"$output" | tr '\n' ' ')"; fi
if device_disconnected "$output"; then root="adb_device_unavailable"; fi
if instrumentation_missing "$output"; then root="instrumentation_not_installed"; fi
printf 'SCENARIO=multi_alarm_stress\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
