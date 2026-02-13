# guardian invariants

These are non-negotiable system guarantees. New features cannot violate them.

## scheduling invariants

- every enabled alarm must have a deterministic trigger plan
- each trigger must map to a unique pending-intent identity
- editing/disabling an alarm must cancel obsolete scheduled triggers

## delivery invariants

- trigger reception must be persisted in history before heavy work
- main triggers must start foreground ringing service immediately
- action handling (`stop`, `snooze`, `join`) must be idempotent

## resilience invariants

- enabled alarms must be rescheduled after reboot
- enabled alarms must be reevaluated after timezone/time changes
- system-risk states must be visible in reliability dashboard

## observability invariants

- each trigger lifecycle event must be traceable in local history
- diagnostics export must include enough context for support triage

## product invariants

- UI cannot directly call AlarmManager or platform scheduling APIs
- domain policy defines trigger generation and nag/escalation behavior
- reliability takes precedence over feature novelty
