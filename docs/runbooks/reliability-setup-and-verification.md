# reliability setup and verification runbook

Use this before test runs, beta rollout, and support triage.

## setup checklist

1. notification permission granted
2. exact alarm capability available
3. battery optimization exemptions applied where needed
4. alarm notification channel configured correctly
5. full-screen alarm behavior verified on lockscreen

## quick verification flow

1. schedule test alarm `+15s`
2. verify receiver is triggered
3. verify foreground service starts
4. verify full-screen ringing UI appears
5. verify stop, snooze, and join actions
6. verify history entries exist for the flow

## reboot verification

1. schedule alarm for `+2m`
2. reboot device before trigger
3. confirm alarm still fires
4. confirm history includes reschedule event

## timezone verification

1. schedule alarm for near future
2. change timezone
3. verify trigger time reflects defined timezone policy
4. confirm reschedule event exists

## issue triage minimum data

- alarm id
- scheduled time
- fired time
- action outcome
- capability states at fire time
- device model and Android version
