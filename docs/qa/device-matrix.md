# qa device matrix

## purpose

Define minimum device coverage for reliability-critical validation.

## required matrix

| segment | example device | android | priority | notes |
|---|---|---:|---:|---|
| stock pixel | Pixel 6/7/8 | 13/14 | P0 | baseline behavior |
| samsung | Galaxy A/S series | 12/13/14 | P0 | common production segment |
| aggressive oem | Tecno/Infinix/Xiaomi | 12/13/14 | P0 | battery/background restrictions |
| lower-end device | 3-4GB RAM class | 11/12 | P1 | memory pressure + process kill risk |
| latest emulator | Android Emulator | latest | P1 | fast regression loop |

## mandatory pass criteria

- stage 4-9 reliability scenarios pass on all `P0` devices.
- no P0 blocker bug remains open.
- all failed scenarios have owner + mitigation + retest date.
