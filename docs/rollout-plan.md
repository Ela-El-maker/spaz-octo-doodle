# guardian rollout plan

## release lanes

1. internal alpha
2. closed beta
3. staged production

## gate criteria per lane

### internal alpha

- all stage 0-9 exit criteria met
- no crash-loop on startup, create, ring, or action flows
- diagnostics export functional

### closed beta

- stage 10 and stage 11 reliability checks complete
- known issues documented with user-visible impact
- support intake process active

### staged production

- permission rationale and policy docs approved
- monitoring dashboards active for crash and ANR
- rollback and hotfix plan validated

## staged production percentages

- `10%` for 24-48 hours
- `50%` for 48-72 hours
- `100%` after stability threshold holds

## stability thresholds

- crash-free sessions meet target for release channel
- no critical severity reliability regressions open
- alarm failure reports are within accepted threshold

## rollback triggers

- critical alarm non-delivery regression
- severe background execution failure across device segment
- high ANR spike linked to ringing or receivers

## post-launch cadence

- daily review during first week
- weekly reliability and incident review afterward
- backlog reprioritized by reliability impact first
