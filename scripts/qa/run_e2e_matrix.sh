#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
source "$ROOT_DIR/scripts/qa/common.sh"

CONNECTED_GATE="false"
if [[ "${1:-}" == "--connected-gate" ]]; then
  CONNECTED_GATE="true"
fi

if ! adb get-state >/dev/null 2>&1; then
  echo "No connected device. Skipping e2e matrix."
  exit 0
fi

if ! adb shell pm list instrumentation | tr -d '\r' | grep -q "com.spazoodle.guardian.test/androidx.test.runner.AndroidJUnitRunner"; then
  echo "Installing app and androidTest packages on connected device..."
  "$ROOT_DIR/gradlew" :app:installDebug :app:installDebugAndroidTest >/dev/null
fi

SCENARIOS=(
  baseline
  dnd
  airplane
  battery_saver
  doze_force_idle
  lock_screen
  lock_screen_action_policy
  stop_guard
  retention_worker
  reboot
  diagnostics_completeness
  multi_alarm_stress
  force_stop
  clear_data
)

JSON_OUT="$ROOT_DIR/docs/qa/reports/latest-e2e.json"
MD_OUT="$ROOT_DIR/docs/qa/reports/latest-e2e.md"
ART_DIR="$ROOT_DIR/docs/qa/reports/artifacts"
mkdir -p "$(dirname "$JSON_OUT")" "$ART_DIR"

echo "{" > "$JSON_OUT"
echo "  \"generatedAt\": \"$(now_iso)\"," >> "$JSON_OUT"
echo "  \"device\": \"$(adb shell getprop ro.product.model | tr -d '\r')\"," >> "$JSON_OUT"
echo "  \"android\": \"$(adb shell getprop ro.build.version.release | tr -d '\r')\"," >> "$JSON_OUT"
echo "  \"results\": [" >> "$JSON_OUT"

cat > "$MD_OUT" <<MD
# Guardian E2E Matrix

- generated: $(now_iso)
- device: $(adb shell getprop ro.product.model | tr -d '\r')
- android: $(adb shell getprop ro.build.version.release | tr -d '\r')

| Scenario | Status | Root Cause | Remediation | Notes |
|---|---|---|---|---|
MD

PASS_COUNT=0
FAIL_COUNT=0
LIMIT_COUNT=0

remediation_for() {
  case "$1" in
    ok) echo "No action required." ;;
    receiver_not_invoked) echo "Verify exact alarm permission, scheduler registration, and receiver manifest export/intent filter." ;;
    service_not_started) echo "Inspect receiver-to-service handoff and foreground service restrictions on this device." ;;
    full_screen_suppressed_oem) echo "Use heads-up fallback, verify full-screen settings, and apply OEM playbook steps." ;;
    force_stop_platform_block) echo "Expected Android behavior. User must relaunch app after force-stop." ;;
    data_clear_reset) echo "Expected Android behavior. Data clear removes alarms; app must be reconfigured." ;;
    late_recovered) echo "Trigger recovered in grace window. Track delay and tune grace/reliability settings." ;;
    missed_beyond_grace) echo "Check doze/idle delays and optimization settings; consider policy tuning." ;;
    dnd_policy_or_delivery_failure) echo "Validate DND alarm allowance and channel interruption settings." ;;
    oem_background_restriction) echo "Apply OEM-specific background/autostart exemptions from reliability dashboard." ;;
    collision_or_dedupe_failure) echo "Inspect trigger key generation, dedupe window, and action idempotency handling." ;;
    diagnostics_incomplete) echo "Confirm history pipeline and diagnostics export include required lifecycle fields." ;;
    instrumentation_not_installed) echo "Install debug and androidTest APKs before running instrumentation scenarios." ;;
    adb_device_unavailable) echo "Reconnect USB device, confirm adb authorization, and rerun matrix." ;;
    lockscreen_policy_failure) echo "Verify require-unlock policy gate in action handler and lock-state detection." ;;
    stop_guard_failure) echo "Verify hold-to-stop interaction state machine and UI semantics." ;;
    retention_worker_not_registered) echo "Ensure startup schedules unique periodic retention work." ;;
    *) echo "Review logs and diagnostics artifacts for root-cause triage." ;;
  esac
}

for s in "${SCENARIOS[@]}"; do
  echo "Running scenario: $s"
  ensure_device_ready >/dev/null 2>&1 || true
  out="$($ROOT_DIR/scripts/qa/scenarios/$s.sh)"
  status="$(grep '^STATUS=' <<<"$out" | cut -d= -f2-)"
  root="$(grep '^ROOT_CAUSE=' <<<"$out" | cut -d= -f2-)"
  notes="$(grep '^NOTES=' <<<"$out" | cut -d= -f2-)"
  remediation="$(remediation_for "$root")"

  case "$status" in
    PASS) PASS_COUNT=$((PASS_COUNT+1));;
    EXPECTED_LIMITATION) LIMIT_COUNT=$((LIMIT_COUNT+1));;
    *) FAIL_COUNT=$((FAIL_COUNT+1));;
  esac

  append_json_result "$JSON_OUT" "$s" "$status" "$root" "$notes" "$remediation"
  printf '| %s | %s | %s | %s | %s |\n' "$s" "$status" "$root" "$remediation" "$notes" >> "$MD_OUT"

done

# drop trailing comma on last result
sed -i '$ s/,$//' "$JSON_OUT"
echo "  ]" >> "$JSON_OUT"
echo "}" >> "$JSON_OUT"

echo "\n## Summary" >> "$MD_OUT"
echo "- pass: $PASS_COUNT" >> "$MD_OUT"
echo "- fail: $FAIL_COUNT" >> "$MD_OUT"
echo "- expected limitation: $LIMIT_COUNT" >> "$MD_OUT"

"$ROOT_DIR/scripts/qa/collect_artifacts.sh" "$ART_DIR"

cat > "$ROOT_DIR/docs/qa/reports/manual-state-scenarios.json" <<MANUAL
{
  "generatedAt": "$(now_iso)",
  "scenarios": [
    {"name": "timezone_change_policy", "status": "MANUAL_REQUIRED", "notes": "Validate fixed-local-time policy and reschedule marker."},
    {"name": "manual_time_change_policy", "status": "MANUAL_REQUIRED", "notes": "Validate time-set handling and late/missed policy behavior."},
    {"name": "device_powered_off_trigger_window", "status": "EXPECTED_LIMITATION", "notes": "Device-off trigger is impossible; verify post-boot recovery notification/history."}
  ]
}
MANUAL

if [[ "$CONNECTED_GATE" == "true" && "$FAIL_COUNT" -gt 0 ]]; then
  echo "Connected gate failed: $FAIL_COUNT failing scenarios"
  exit 2
fi

echo "Matrix complete. JSON: $JSON_OUT MD: $MD_OUT"
