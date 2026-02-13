# adr-0001: architecture and reliability-first policy

## status

accepted

## context

Guardian is an alarm-grade reminder system. Reliability under real Android constraints is the primary product requirement.

## decision

- use layered boundaries: `ui`, `domain`, `data`, `platform`, `receiver`, `service`
- use trigger abstraction for all timed events (`main`, `pre-alert`, `snooze`, `nag`)
- persist all key lifecycle events to local history for diagnostics
- use exact scheduling path as primary for time-critical triggers
- prioritize delivery guarantees over feature expansion

## consequences

### positive

- predictable flow and easier debugging
- cleaner test boundaries and lower regression risk
- scalable feature additions using same trigger model

### tradeoffs

- more upfront structure before rapid feature iteration
- stricter engineering discipline for platform boundaries
