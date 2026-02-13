# stage 3 checklist

## objective

Make persistence durable for alarms and lifecycle history, with migration support.

## implemented

- [x] Extended `AlarmDao` with upcoming alarms query.
- [x] Added `AlarmHistoryEntity` and indexed history table structure.
- [x] Added `AlarmHistoryDao` for append/query operations.
- [x] Added `AlarmHistoryRepositoryImpl` implementing domain history contract.
- [x] Extended `AlarmRepositoryImpl` with upcoming alarms query support.
- [x] Upgraded `GuardianDatabase` to version `2`.
- [x] Added migration `MIGRATION_1_2` covering:
  - new alarm policy/snooze columns
  - `alarm_history` table creation
  - required indexes
- [x] Added `GuardianDatabaseFactory` with migration registration.
- [x] Added domain repository contract support for upcoming query.

## verification

- Domain tests executed:
  - `./gradlew :core-domain:test --console=plain`
- Android app DB instrumentation tests are pending due local SDK Build-Tools corruption in this environment.

## known follow-up

- Stage 4 will bind domain schedule plans to platform scheduler APIs and trigger identity strategy.
- Stage 5 will wire receiver flows to history repository writes.
