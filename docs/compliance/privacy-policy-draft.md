# privacy policy draft

## summary

Guardian stores user-created plans and alarm history locally on-device to provide reliable reminders.

## data we process

- plan metadata: title, time, timezone, policy settings, optional primary action values.
- operational history: scheduled/fired/outcome timestamps and diagnostic details.
- device capability state (runtime): notification/exact-alarm/battery optimization checks.

## storage

- primary storage: on-device Room database.
- diagnostics export: generated on demand by user and shared only via explicit user action.

## data sharing

- no automatic third-party sharing is performed by default.
- if analytics/crash tooling is added later, explicit policy update and consent model are required.

## retention

- plan and history retention follows app behavior until user edits/deletes entries.
- future enhancement may include retention controls (e.g., auto-prune history older than N days).

## user controls

- users can disable plans, remove plans, and control permissions in system settings.
- users can copy/share diagnostics manually.

## security baseline

- principle of least data: collect only what is needed for reliable delivery and diagnostics.
- avoid storing unrelated sensitive content unless explicitly required by feature.

## contact and updates

- policy owner: project maintainers.
- policy revision required for any new external data flow or telemetry integration.
