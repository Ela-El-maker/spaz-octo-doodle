# defect triage

## severity model

- `P0`: user misses critical trigger; reliable delivery compromised.
- `P1`: alarm fires but key action path broken.
- `P2`: non-critical behavior defect.
- `P3`: cosmetic or low-impact issue.

## required fields for every issue

- device + android version
- app build/version
- scenario id
- expected behavior
- actual behavior
- diagnostics payload
- reproduction steps
- owner + target fix milestone

## sla targets

- `P0`: same day mitigation, hotfix priority
- `P1`: next patch cycle
- `P2/P3`: planned backlog
