#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUT_DIR="${1:-$ROOT_DIR/docs/qa/reports/artifacts}"
mkdir -p "$OUT_DIR"

adb shell getprop > "$OUT_DIR/device_getprop.txt" || true
adb shell dumpsys battery > "$OUT_DIR/device_battery.txt" || true
adb shell settings list global > "$OUT_DIR/settings_global.txt" || true
adb shell settings list secure > "$OUT_DIR/settings_secure.txt" || true
adb shell dumpsys notification > "$OUT_DIR/notification_dump.txt" || true
adb shell dumpsys activity services com.spazoodle.guardian > "$OUT_DIR/services_guardian.txt" || true
adb logcat -d -v time -t 1200 > "$OUT_DIR/logcat_tail.txt" || true

echo "Collected artifacts in $OUT_DIR"
