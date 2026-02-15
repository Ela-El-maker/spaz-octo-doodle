#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
set_doze_force_idle
output="$(run_inst_test com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest || true)"
clear_doze_force_idle
status="$(result_from_output "$output")"
root="ok"
notes="force doze"
if [[ "$status" != "PASS" ]]; then root="missed_beyond_grace"; notes="$(tail -n 8 <<<"$output" | tr '\n' ' ')"; fi
if device_disconnected "$output"; then root="adb_device_unavailable"; fi
if instrumentation_missing "$output"; then root="instrumentation_not_installed"; fi
printf 'SCENARIO=doze_force_idle\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
