# incident response runbook

## purpose

Standardize response when reliability-critical regressions are detected during rollout.

## severity

- `SEV-1`: missed critical triggers widespread
- `SEV-2`: action path broken for significant cohort
- `SEV-3`: non-critical degradation

## first 30 minutes

1. freeze rollout promotion
2. classify severity and impacted versions/devices
3. collect representative diagnostics exports
4. assign incident commander and owners
5. publish internal incident status note

## triage flow

1. reproduce against scenario id from `docs/qa/test-scenarios.md`
2. verify if issue is config-only or binary regression
3. decide mitigation path:
- rollback
- hotfix patch
- user guidance + dashboard fix
4. define verification criteria for resolution

## communication cadence

- `SEV-1`: updates every 30 minutes
- `SEV-2`: updates every 60 minutes
- `SEV-3`: updates every 24 hours

## closure criteria

- root cause documented
- mitigation shipped and verified
- monitoring trends returned to baseline
- follow-up tasks filed with owners and due dates
