# guardian user flow

This maps what users see to what the system does in the background.

## stage 1: first launch and trust setup

### user sees

- welcome and reliability setup checklist
- optional test alarm in 15 seconds

### background

- create notification channels
- inspect runtime capability states
- persist defaults in DataStore
- run scheduler test pipeline when requested

## stage 2: creating a guardian meeting alarm

### user sees

- create form for title, date, time, meeting link
- toggles for pre-alerts, nag mode, escalation

### background

- validate request in view model
- convert time and timezone in domain use case
- persist alarm in Room
- compute triggers and schedule exact alarms

## stage 3: list and idle period

### user sees

- alarm listed with date/time and status badge
- enable/disable toggle

### background

- no heavy background loops
- optional lightweight health checks

## stage 4: pre-alert fires

### user sees

- reminder notification (non full-screen)

### background

- broadcast received
- alarm state validated
- pre-alert history written
- reminder notification posted

## stage 5: main alarm fires

### user sees

- full-screen ringing UI
- stop, snooze, join meeting actions

### background

- receiver logs main fire
- foreground service starts playback and vibration
- high-priority notification remains active

## stage 6: user action

### user sees

- stop: alarm ends
- snooze: next ring time shown
- join: meeting app/browser opens

### background

- action routed through use case
- history updated with outcome
- snooze trigger scheduled when applicable
- service shuts down cleanly

## stage 7: no user action and nag mode

### user sees

- repeated rings based on policy
- optional escalation behavior

### background

- nag trigger chain scheduled until acknowledged or max reached
- each cycle logged with count and outcome

## stage 8: post-event proof

### user sees

- history record with scheduled/fired/action times
- reliability health state

### background

- final event timeline stored in Room
- diagnostics export data prepared

## stage 9: messy-world edge cases

### reboot

- boot receiver reloads enabled alarms and reschedules

### timezone/time change

- time-change receiver recomputes schedules based on policy

### capability degradation

- dashboard marks alarms at risk and guides fixes
