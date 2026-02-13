# guardian overview

## what this system is

Guardian is an Android alarm system that schedules exact, far-future date-time alarms and delivers them with alarm-grade reliability.

It supports:

- one-shot date-time alarms
- meeting alarms with join links
- pre-alert bundles
- snooze and nag flows
- proof/history for fired and acknowledged events

## problem it solves

Typical reminder systems fail under real device conditions: Doze, reboot, timezone changes, aggressive battery policies, and notification misconfiguration.

Guardian focuses on delivery guarantees:

- schedule exactly
- survive system changes
- ring in a way users can act on
- keep audit history for trust and support

## local run

1. Build with `./gradlew :app:assembleDebug`
2. Install with `./gradlew :app:installDebug`
3. Launch from device app drawer (`Guardian`)

Stage 1 Android foundation is now scaffolded. The remaining stages are tracked in `docs/development-stages.md`.
