# staged rollout playbook

## objective

Execute safe production rollout with clear promotion/rollback decisions.

## rollout lanes

1. internal alpha
2. closed beta
3. staged production

## staged production percentages

1. 10% for 24-48 hours
2. 50% for 48-72 hours
3. 100% after stability gate holds

## promotion gates per step

- crash and ANR trends within threshold
- no open `P0` reliability defect
- alarm failure reports within accepted threshold
- support queue trend stable

## rollback triggers

- critical non-delivery regression
- severe background execution failure in major segment
- ANR spike linked to receiver/service/action loop

## execution checklist

1. confirm monitoring dashboard is active
2. confirm on-call owners are available
3. publish release note with known limits and mitigations
4. start rollout at configured percentage
5. review metrics every 4 hours in first 24h
6. decide promote/hold/rollback
7. log decision and rationale
