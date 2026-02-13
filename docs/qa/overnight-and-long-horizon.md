# overnight and long-horizon protocol

## overnight protocol

1. schedule 3 plans for overnight windows:
- pre-alert only
- main only
- main + nag policy
2. set device idle state (screen off, unplugged).
3. do not open app overnight.
4. in morning verify:
- all expected fires occurred
- outcomes recorded
- no stuck foreground service

## long-horizon protocol

1. schedule plans at +7d, +30d, +60d.
2. edit one plan after scheduling.
3. disable/enable one plan.
4. reboot device once in between.
5. verify history + scheduler consistency.

## failure classification

- `critical`: plan did not fire with no user-action cause.
- `major`: fired but action loop broken or history missing.
- `minor`: cosmetic or non-blocking UI mismatch.
