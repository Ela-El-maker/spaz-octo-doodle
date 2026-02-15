# QA Device Matrix

## Purpose
Define minimum device coverage for reliability-critical validation.

## Required Matrix

| segment | example device | android | priority | notes |
|---|---|---:|---:|---|
| stock pixel | Pixel 6/7/8 | 13/14 | P0 | baseline behavior |
| samsung | Galaxy A/S series | 12/13/14 | P0 | common production segment |
| aggressive oem | Tecno/Infinix/Xiaomi | 12/13/14 | P0 | battery/background restrictions |
| lower-end device | 3-4GB RAM class | 11/12 | P1 | memory pressure + process kill risk |
| latest emulator | Android Emulator | latest | P1 | fast regression loop |

## Mandatory Pass Criteria

- all non-impossible matrix scenarios are `PASS` on every `P0` segment.
- no `FAIL` remains for `P0`.
- `EXPECTED_LIMITATION` only allowed for:
  - force-stop with pending alarm.
  - clear-data with pending alarm.
  - powered-off trigger window (must verify post-boot recovery path).
- all failures include root-cause tag, owner, mitigation, and retest date.
