# qa test scenarios

## scope

These scenarios are required before beta/production promotion.

## scenario set

1. exact trigger near-term
- create plan +2 minutes
- verify fire time drift
- verify stop, snooze, do actions

2. pre-alert chain
- create plan with pre-alert offsets
- verify pre-alert notifications fire in order

3. reboot resilience
- schedule +3 minutes
- reboot before trigger
- verify reschedule + fire

4. timezone/time change resilience
- schedule near future
- change timezone
- verify policy-correct trigger time

5. app update resilience
- schedule +3 minutes
- install updated build
- verify trigger persists

6. doze/idle overnight
- schedule overnight trigger
- leave device idle
- verify morning fire and history

7. dnd + alarm policy behavior
- enable dnd modes
- verify alarm behavior + user guidance paths

8. battery optimization stress
- enforce optimized mode then unrestricted mode
- verify reliability dashboard status transitions

9. long-horizon scheduling
- schedule 30+ days ahead
- verify persistence + history + edit/cancel flows

10. diagnostics completeness
- trigger failure simulation
- verify export contains required fields

## required artifacts per scenario

- pass/fail
- device + os version
- expected vs actual
- diagnostics payload
- screenshot/video if failed
