# Guardian Testing Guide

## Build and Install

- Build debug APK: `./gradlew :app:assembleDebug`
- Install debug APK: `./gradlew :app:installDebug`

## Automated Tests

- Domain tests: `./gradlew :core-domain:test`
- Instrumented tests (connected device required): `./gradlew :app:connectedDebugAndroidTest`

## Manual Reliability Checks

Run these on a physical device:

1. Baseline alarm fires at exact scheduled time.
2. Reboot before trigger, verify reschedule and delivery.
3. Timezone/manual time change, verify policy handling.
4. DND and battery optimization behavior.
5. Lock screen ringing and action behavior.
6. Multi-alarm near-time stress.

## Expected Limitations

- Force-stop from system settings blocks alarm delivery until relaunch.
- Clearing app data removes all schedules.
- Powered-off devices cannot ring at trigger instant.

These cases should be documented and surfaced to the user, not treated as silent failures.
