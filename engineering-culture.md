# Engineering Culture

## Purpose

This document defines the **baseline engineering culture** for this repository.

It exists to ensure:

- consistency across projects
- predictable structure
- readable naming
- discoverable documentation

It is intentionally minimal.

This culture is **not** intended to fight frameworks, slow contributors, or impose unnecessary architectural purity.  
Its goal is clarity, not ceremony.

---

## Scope

This culture applies to:

- repository naming
- top-level project structure
- documentation layout
- naming conventions

Framework-specific conventions (Laravel, Django, Rails, etc.) are respected and preserved **inside the framework boundary**.

---

## 1. Naming Conventions

### 1.1 General Rules

All project-owned names follow these rules:

- lowercase only
- kebab-case for repositories and directories
- avoid technology names when reasonable
- prefer names based on **responsibility**, not implementation

Examples:

- `quoodle-control-plane` ✅
- `telemetry-ingest` ✅
- `user-auth-fastapi` ❌ (tech in name without necessity)
- `backend-api` ❌ (vague responsibility)

Exceptions are allowed when:

- the technology itself is the point of the project
- the project is explicitly experimental or educational

---

### 1.2 Repository Naming

Repository names should describe **what the system does**, not how it is built.

Pattern (guideline, not mandate):

<ecosystem>-<domain>-<role>

Examples:

- `quoodle-gateway`
- `zingoodle-memory`
- `policy-engine`

---

## 2. Repository Structure

### 2.1 Required Top-Level Structure

Every repository must contain at least:

README.md
docs/

Optional but common:
scripts/
tests/

### 2.2 Meaning of Top-Level Directories

- `docs/`  
  All project documentation lives here.

- `scripts/`  
  Developer automation, setup, or helper scripts.

- `tests/`  
  Automated tests (unit, integration, or E2E).

This culture **wraps frameworks**, it does not fight them.

---

## 4. Documentation Rules

### 4.1 Mandatory Documentation

Every project must include at least:

docs/
overview.md
architecture.md

#### `overview.md`

Must answer:

- what the system is
- what problem it solves
- how to run it locally

#### `architecture.md`

Must describe:

- major components
- data or control flow
- important boundaries or assumptions

Markdown is the default documentation format.

---

### 4.2 Additional Documentation (Optional but Encouraged)

Depending on project complexity, the following may be added:

- `decisions/` – design or architectural decisions
- `invariants.md` – system guarantees or assumptions
- `threat-model.md` – for security-sensitive systems
- `runbooks/` – operational notes

---

## 5. Formatting & Style

- Formatting and linting tools provided by the project must be used.
- Automated formatters take precedence over manual style preferences.
- Consistency is more important than personal taste.

---

## 6. Contribution Philosophy

This culture is designed to be:

- easy to understand
- easy to follow
- easy to explain to new contributors

If a rule causes repeated confusion or friction, it should be revisited.

The goal is **shared understanding**, not rigidity.

---

## 7. Summary

This engineering culture enforces only what matters:

- predictable naming
- consistent structure
- centralized documentation
- respect for framework conventions

Everything else is intentionally left flexible.

Clarity over cleverness.  
Consistency over novelty.  
Documentation over assumption.
