# store release readiness

## listing and disclosure checklist

- app description reflects reliability-first reminder behavior.
- permission rationale references `docs/compliance/permissions-rationale.md`.
- privacy policy text is published from `docs/compliance/privacy-policy-draft.md`.
- screenshots include create/list/ringing/reliability/history screens.

## policy-sensitive declarations

- foreground service purpose documented as alarm playback/reliability.
- exact alarm usage justified as core functional requirement.
- boot receiver behavior explained in user-facing FAQ/support docs.

## pre-launch checks

- release smoke completed (`docs/qa/release-smoke.md`).
- P0 matrix scenarios passed (`docs/qa/device-matrix.md`, `docs/qa/test-scenarios.md`).
- no open P0 defects (`docs/qa/defect-triage.md`).

## release candidate gate

1. permissions rationale approved
2. privacy policy approved
3. monitoring checklist completed
4. rollback triggers validated
5. staged rollout plan confirmed
