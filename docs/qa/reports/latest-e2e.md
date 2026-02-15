# Guardian E2E Matrix

- generated: 2026-02-14T19:44:34Z
- device: Infinix X658E
- android: 11

| Scenario | Status | Root Cause | Remediation | Notes |
|---|---|---|---|---|
| baseline | PASS | ok | No action required. | real pipeline integration |
| dnd | PASS | ok | No action required. | dnd none |
| airplane | FAIL | receiver_not_invoked | Verify exact alarm permission, scheduler registration, and receiver manifest export/intent filter. |  com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest:  |
| battery_saver | PASS | ok | No action required. | battery saver |
| doze_force_idle | PASS | ok | No action required. | force doze |
| lock_screen | PASS | ok | No action required. | lock screen |
| lock_screen_action_policy | PASS | ok | No action required. | lock-screen unlock-required policy |
| stop_guard | PASS | ok | No action required. | hold-to-stop guard |
| retention_worker | PASS | ok | No action required. | retention worker registration |
| reboot | FAIL | receiver_not_invoked | Verify exact alarm permission, scheduler registration, and receiver manifest export/intent filter. | 	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:59) 	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source:1) 	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:38) 	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source:1) 	at com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest.createdAlarm_shouldFireMainTrigger_andBeRecordedInHistory(RealAlarmPipelineIntegrationTest.kt:24)  FAILURES!!! Tests run: 1,  Failures: 1  |
| diagnostics_completeness | FAIL | diagnostics_incomplete | Confirm history pipeline and diagnostics export include required lifecycle fields. | 	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33) 	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108) 	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:280) 	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:85) 	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:59) 	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source:1) 	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:38) 	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source:1) 	at com.spazoodle.guardian.ui.RealAlarmPipelineIntegrationTest.createdAlarm_shouldFireMainTrigger_andBeRecordedInHistory(RealAlarmPipelineIntegrationTest.kt:24)  FAILURES!!! Tests run: 1,  Failures: 1  |
| multi_alarm_stress | FAIL | collision_or_dedupe_failure | Inspect trigger key generation, dedupe window, and action idempotency handling. | 	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33) 	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:108) 	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:280) 	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:85) 	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:59) 	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source:1) 	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:38) 	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source:1) 	at com.spazoodle.guardian.ui.MultiAlarmStressIntegrationTest.multipleNearTermAlarms_shouldAllFireMain(MultiAlarmStressIntegrationTest.kt:23)  FAILURES!!! Tests run: 2,  Failures: 1  |
| force_stop | EXPECTED_LIMITATION | force_stop_platform_block | Expected Android behavior. User must relaunch app after force-stop. | force-stop cancels delivery by Android design |
| clear_data | EXPECTED_LIMITATION | data_clear_reset | Expected Android behavior. Data clear removes alarms; app must be reconfigured. | data clear removes schedules and app state |
\n## Summary
- pass: 8
- fail: 4
- expected limitation: 2
