# project setup compliance

This checklist enforces `engineering-culture.md` before feature development starts.

## structure compliance

- required root docs exist:
  - `README.md`
  - `docs/`
- mandatory docs exist:
  - `docs/overview.md`
  - `docs/architecture.md`

## naming compliance

- documentation directories use lowercase and kebab-case
- decision records use stable `adr-xxxx-...` naming
- new project-owned directories and files follow lowercase naming

## documentation layout compliance

- architecture and flow are documented before implementation
- stage plan and rollout criteria are documented
- invariants are documented and treated as release gates

## contribution compliance

- contributors must update docs when behavior changes
- reliability-impacting changes require:
  - invariant review
  - runbook update
  - stage plan update if scope changes

## gate to begin development

Development starts only when:

1. this checklist is satisfied
2. scope and stages are approved
3. initial ADR is accepted
