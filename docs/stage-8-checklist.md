# stage 8 checklist

## objective

Implement a reliability dashboard that surfaces risk states, provides guided fix actions, and supports a live test alarm path.

## implemented

- [x] Added reliability scanning layer:
  - `ReliabilityScanner`
  - computes checks for:
    - notifications enabled
    - exact alarm capability
    - battery optimization status
    - full-screen channel readiness
    - DND interruption state (best-effort)
  - computes health score (`0..100`)
- [x] Added reliability UI state and view model:
  - `ReliabilityUiState`
  - `ReliabilityViewModel`
  - actions:
    - refresh checks
    - open notification settings
    - open exact alarm settings
    - open battery optimization settings
    - open DND policy settings
    - schedule test alarm in 15 seconds
- [x] Added reliability dashboard screen:
  - `ReliabilityScreen`
  - status rows with `Fix` actions for failing checks
  - health score display
  - test alarm button
- [x] Added navigation route:
  - `reliability`
- [x] Added home entry point:
  - `Reliability Dashboard` button on home screen

## verification

- Domain verification command:
  - `./gradlew :core-domain:test --console=plain`
- Result: `BUILD SUCCESSFUL`

## known follow-up

- App-level compile/runtime verification remains blocked by local SDK Build-Tools corruption in this environment.
- Stage 9 will implement history/proof timeline and diagnostics export screen.
