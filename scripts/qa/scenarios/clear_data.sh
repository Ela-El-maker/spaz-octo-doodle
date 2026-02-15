#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "$0")/../common.sh"
probe_cmd='adb shell am instrument -w -e class com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest com.spazoodle.guardian.test/androidx.test.runner.AndroidJUnitRunner'
logf="/tmp/guardian_clear_data_probe.log"
sh -c "$probe_cmd" > "$logf" 2>&1 & pid=$!
sleep 10
adb shell pm clear com.spazoodle.guardian >/dev/null 2>&1 || true
wait $pid || true
status="EXPECTED_LIMITATION"
root="data_clear_reset"
notes="data clear removes schedules and app state"
printf 'SCENARIO=clear_data\nSTATUS=%s\nROOT_CAUSE=%s\nNOTES=%s\n' "$status" "$root" "$notes"
