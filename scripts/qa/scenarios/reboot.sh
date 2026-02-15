#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
adb reboot >/dev/null 2>&1 || true
adb wait-for-device >/dev/null 2>&1
sleep 15
output="$(run_inst_test com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest || true)"
status="$(result_from_output "$output")"
root="ok"
notes="post reboot"
if [[ "$status" != "PASS" ]]; then root="receiver_not_invoked"; notes="$(tail -n 8 <<<"$output" | tr '\n' ' ')"; fi
if device_disconnected "$output"; then root="adb_device_unavailable"; fi
if instrumentation_missing "$output"; then root="instrumentation_not_installed"; fi
printf 'SCENARIO=reboot\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
