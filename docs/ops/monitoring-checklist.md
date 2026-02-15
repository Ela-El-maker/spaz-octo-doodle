# monitoring checklist

## purpose

Define minimum monitoring and response readiness before staged production rollout.

## required signals

- crash-free sessions trend
- ANR rate trend
- alarm failure reports count
- diagnostics export issue volume
- reliability dashboard low-score cohort percentage

## alert thresholds

- crash-free sessions drops below target threshold
- ANR spike above configured baseline
- sudden increase in missed-trigger reports

## response workflow

1. identify affected app version and device segment
2. inspect diagnostics payload samples
3. classify issue severity using `docs/qa/defect-triage.md`
4. decide mitigation: config guidance, hotfix, or rollback
5. update rollout percentage according to risk

## ownership

- release owner: coordinates go/no-go decisions
- reliability owner: validates trigger-path regressions
- support owner: consolidates user reports and diagnostics

## readiness gate

- all required signals observable
- response workflow rehearsed
- ownership assigned with on-call coverage
