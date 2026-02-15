#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
APP_PKG="com.spazoodle.guardian"
TEST_RUNNER="com.spazoodle.guardian.test/androidx.test.runner.AndroidJUnitRunner"

now_iso() {
  date -u +"%Y-%m-%dT%H:%M:%SZ"
}

ensure_device() {
  adb get-state >/dev/null 2>&1
}

ensure_device_ready() {
  adb wait-for-device >/dev/null 2>&1 || true
  for _ in $(seq 1 90); do
    local boot
    boot="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
    if [[ "$boot" == "1" ]]; then
      return 0
    fi
    sleep 2
  done
  return 1
}

run_inst_test() {
  local klass="$1"
  ensure_device_ready >/dev/null 2>&1 || true
  adb shell am instrument -w -e class "$klass" "$TEST_RUNNER" 2>&1
}

instrumentation_missing() {
  local output="$1"
  grep -q "Unable to find instrumentation info" <<<"$output"
}

device_disconnected() {
  local output="$1"
  grep -q "no devices/emulators found" <<<"$output"
}

set_dnd_off() {
  adb shell cmd notification set_dnd off >/dev/null 2>&1 || true
}

set_airplane() {
  local on="$1"
  adb shell settings put global airplane_mode_on "$on" >/dev/null 2>&1 || true
  if [[ "$on" == "1" ]]; then
    adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true >/dev/null 2>&1 || true
  else
    adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false >/dev/null 2>&1 || true
  fi
}

set_battery_saver() {
  local on="$1"
  adb shell settings put global low_power "$on" >/dev/null 2>&1 || true
}

set_doze_force_idle() {
  adb shell dumpsys battery unplug >/dev/null 2>&1 || true
  adb shell dumpsys deviceidle force-idle >/dev/null 2>&1 || true
}

clear_doze_force_idle() {
  adb shell dumpsys deviceidle unforce >/dev/null 2>&1 || true
  adb shell dumpsys battery reset >/dev/null 2>&1 || true
}

lock_screen() {
  adb shell input keyevent 26 >/dev/null 2>&1 || true
  adb shell input keyevent 223 >/dev/null 2>&1 || true
}

unlock_screen() {
  adb shell input keyevent 224 >/dev/null 2>&1 || true
}

result_from_output() {
  local output="$1"
  if grep -q "OK (" <<<"$output"; then
    echo "PASS"
  else
    echo "FAIL"
  fi
}

append_json_result() {
  local file="$1"
  local scenario="$2"
  local status="$3"
  local root_cause="$4"
  local notes="$5"
  local remediation="${6:-}"
  local escaped_notes escaped_root escaped_remediation
  escaped_notes=$(printf '%s' "$notes" | sed 's/"/\\"/g')
  escaped_root=$(printf '%s' "$root_cause" | sed 's/"/\\"/g')
  escaped_remediation=$(printf '%s' "$remediation" | sed 's/"/\\"/g')
  printf '  {"scenario":"%s","status":"%s","root_cause":"%s","notes":"%s","remediation":"%s"},\n' \
    "$scenario" "$status" "$escaped_root" "$escaped_notes" "$escaped_remediation" >> "$file"
}
