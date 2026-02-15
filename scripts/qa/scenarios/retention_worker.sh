#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
output="$(run_inst_test com.spazoodle.guardian.qa.RetentionWorkerRegistrationTest || true)"
status="$(result_from_output "$output")"
root="ok"
notes="retention worker registration"
if [[ "$status" != "PASS" ]]; then root="retention_worker_not_registered"; notes="$(tail -n 10 <<<"$output" | tr '\n' ' ')"; fi
if device_disconnected "$output"; then root="adb_device_unavailable"; fi
if instrumentation_missing "$output"; then root="instrumentation_not_installed"; fi
printf 'SCENARIO=retention_worker\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
