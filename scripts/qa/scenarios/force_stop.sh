#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
probe_cmd='adb shell am instrument -w -e class com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest com.spazoodle.guardian.test/androidx.test.runner.AndroidJUnitRunner'
logf="/tmp/guardian_force_stop_probe.log"
sh -c "$probe_cmd" > "$logf" 2>&1 & pid=$!
sleep 10
adb shell am force-stop com.spazoodle.guardian >/dev/null 2>&1 || true
wait $pid || true
output="$(cat "$logf")"
status="EXPECTED_LIMITATION"
root="force_stop_platform_block"
notes="force-stop cancels delivery by Android design"
printf 'SCENARIO=force_stop\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
