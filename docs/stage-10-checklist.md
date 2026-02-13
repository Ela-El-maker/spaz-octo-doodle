# stage 10 checklist

## objective

Implement advanced feature pack without reducing reliability: templates, configurable nag/escalation, and smarter action defaults.

## implemented

- [x] Added reusable plan templates in editor flow:
  - `STANDARD`
  - `CRITICAL`
  - `TRAVEL`
  - `QUIET`
- [x] Added template application logic in `HomeViewModel`:
  - adjusts pre-alert bundle
  - adjusts nag defaults
  - adjusts escalation defaults
- [x] Added advanced nag controls in editor UI:
  - repeat minutes CSV
  - max nag count
  - max nag window minutes
  - escalation enable toggle
  - escalation step-after count
- [x] Wired nag/escalation controls into alarm policy generation.
- [x] Added primary action type quick picks with smart default labels.
- [x] Extended draft model with stage-10 fields for template + advanced policy tuning.

## verification

- Domain verification command:
  - `./gradlew :core-domain:test --console=plain`
- Result: `BUILD SUCCESSFUL`

## known follow-up

- App-level compile/runtime verification remains blocked by local SDK Build-Tools corruption in this environment.
- Next stage focuses on QA hardening and device matrix validation flow.
