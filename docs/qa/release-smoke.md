# release smoke checklist

Run on release build (`release` variant) before rollout.

## smoke list

1. app launch and navigation
2. create/edit/disable/enable plan
3. schedule +15s test trigger
4. stop, snooze, do action from full-screen + notification
5. reliability dashboard checks + fix deep-links
6. history timeline updates in real time
7. diagnostics copy/share
8. reboot reschedule sanity check

## pass gate

- all smoke items pass on at least one `P0` physical device.
- no crash/ANR during smoke run.
