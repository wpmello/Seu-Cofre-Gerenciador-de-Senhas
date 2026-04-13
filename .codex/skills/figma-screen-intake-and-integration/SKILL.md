---
name: figma-screen-intake-and-integration
description: Use when the user provides a Figma screen/frame/link for an Android app screen, or asks to create/refine a screen using the existing design system and integrate it with the current app flow, domain, navigation, persistence, and tests. Do not use for backend-only tasks or for generic UI work that has no screen-level product/design context.
---

# Figma Screen Intake and Integration

## Purpose

Use this skill when a task starts from a Figma screen, frame, screen link, or a request to create a new Android screen that must match the project's established design system and existing flows.

This skill turns a screen request into a full implementation workflow:
1. inspect the screen and extract visible requirements
2. compare the new screen with existing screens, navigation, domain models, and persisted data
3. identify reuse opportunities before creating anything new
4. implement the UI in Android native Jetpack Compose
5. integrate state, navigation, domain, persistence, and tests
6. update docs if the screen changes product contracts or architecture-relevant decisions

## Required project context

Before implementation, read and follow these project files when present:
- `AGENTS.md`
- `docs/architecture.md`
- `docs/testing.md`
- `docs/security.md`
- `docs/ui-source-of-truth-figma-compose.md`
- `docs/figma-screen-intake-and-integration.md`
- `docs/design-system-operational-source-of-truth.md`
- `docs/design-system-stitch-origin.md` or `docs/DESIGN.md`, if present

## Source priority

Follow this source precedence:
1. exact Figma frame/screen given in the task
2. tokens, components, variants, and neighboring states in the same Figma file
3. `docs/ui-source-of-truth-figma-compose.md`
4. `docs/design-system-operational-source-of-truth.md`
5. original Stitch design source (`docs/design-system-stitch-origin.md` or `docs/DESIGN.md`)
6. existing code patterns in the repo
7. official Android/Compose best practices

Do not let the generic design system override an approved Figma screen.

## When to use this skill

Use this skill when:
- the prompt contains a Figma link, frame, or screen reference
- the user asks to implement a new screen from approved Figma designs
- the user asks to add a new screen that has no Figma but should follow the existing app design system
- the new screen must be integrated with current flows, state, domain, navigation, or tests

Do not use this skill when:
- the task is backend-only
- the task is a pure bugfix unrelated to screen contracts or screen integration
- the task is only about infrastructure, CI, Gradle, or documentation unrelated to screen behavior

## Non-negotiable rules

- Build Android native UI with Kotlin + Jetpack Compose.
- Use Material 3 as the technical base, not as permission to redesign the screen.
- Use Navigation Compose when navigation changes are needed.
- Use Hilt for dependency injection.
- Use Room/DataStore/coroutines/Flow according to project architecture.
- Do not invent business rules that are not justified by the screen, docs, or current code.
- Do not expose sensitive data in previews, logs, sample state, screenshots, or test fixtures.
- Do not create parallel navigation, parallel state models, or ad-hoc persistence shortcuts.
- Prefer reuse over new components.
- Keep screen contracts explicit: state, actions/events, and effects when needed.

## Workflow

### Step 1 — Intake the screen

Extract from the Figma screen or request:
- screen name
- purpose
- entry points and exit points
- visible fields and values
- visible actions
- empty/loading/error/success/sensitive states
- copy/text that appears user-facing
- domain hints (for example: timestamps, categories, security indicators, metadata)
- persistence hints (for example: stable values shown as part of product behavior)
- navigation hints (for example: detail → edit → confirm delete)
- security hints (for example: reveal secret, biometric gate, copy secret)

If a field appears stable and product-relevant in the UI, treat it as a candidate formal requirement for domain/persistence/tests.

### Step 2 — Compare against current project state

Inspect the codebase and docs before coding:
- existing screens and flows
- navigation graph
- current feature package and naming conventions
- current domain models and repository contracts
- Room entities and DAO queries
- DataStore keys/settings if relevant
- existing tests and feature specs
- reusable components, tokens, and theme objects

Determine whether the new screen:
- extends an existing flow
- requires a new destination
- changes an existing entity/model
- introduces a new state or action that affects tests
- requires documentation updates

### Step 3 — Decide reuse vs creation

Prefer this order:
1. reuse an existing screen/component contract
2. extend an existing component minimally
3. create a new component only when existing pieces do not fit cleanly

If creating new domain or persistence fields because the screen requires them, update all affected layers deliberately.

### Step 4 — Define the screen contract

Before coding, define:
- `UiState`
- UI actions/events
- transient effects only when required
- route/screen/component boundaries
- navigation callbacks
- dependency and use case touchpoints
- which state is UI-local and which state belongs in the ViewModel

Use explicit, immutable screen state.

### Step 5 — Implement UI first, then integrate

Implementation order:
1. screen skeleton and layout structure
2. reusable components and tokens
3. previews with safe fake data
4. route/viewmodel binding
5. navigation integration
6. domain/use case/repository updates if needed
7. persistence updates if needed
8. tests

Avoid mixing all layers in the first pass.

### Step 6 — Update related layers when the screen proves a requirement

If the screen makes a requirement visible and stable, reflect it across layers where appropriate:
- domain model
- Room entity
- mapper(s)
- repository contract/implementation
- use cases
- UI state
- tests
- docs

Example: if the detail screen shows `createdAt` and `updatedAt` as product-level metadata, those fields likely belong in domain, persistence, mappers, and tests.

### Step 7 — Testing obligations

When a screen is added or changed, update tests relevant to the behavior:
- ViewModel/state tests
- Compose UI tests for visible behavior
- mapper tests if contracts changed
- repository/use case tests if domain or persistence changed
- security-sensitive tests if the screen reveals/copies/deletes/exports secrets

At minimum, derive tests from:
- visible states
- user actions
- screen-specific error paths
- navigation outcomes
- sensitive-data handling rules

### Step 8 — Documentation obligations

Update docs when the screen introduces or changes:
- product-visible fields that imply domain/persistence requirements
- architectural rules or layering decisions
- new navigation flows
- testing strategy for a new class of interaction
- design-system rules not already documented

Do not update docs for every tiny UI tweak. Document only when the change affects shared understanding, shared contracts, or future implementation decisions.

## Implementation checklist

Before finishing, verify:
- the new screen matches the Figma/design-system source used for the task
- the screen fits the current navigation model
- the state model is explicit and predictable
- there is no direct infrastructure access from Compose UI
- sensitive data is masked/guarded appropriately
- strings are localized through resources
- previews use safe fake data only
- tests cover the new or changed behavior
- docs were updated if screen contracts changed

## Definition of done

A Figma-driven screen task is done only when:
- screen requirements were extracted before coding
- relationships with existing screens/flows were checked
- reuse opportunities were considered first
- UI was implemented in Compose and integrated with project architecture
- navigation/state/domain/persistence were updated where required
- relevant tests were added or updated and pass
- build/test verification required by `AGENTS.md` was completed
- documentation was updated when the screen changed shared contracts or project guidance

## Output expectations

When using this skill, produce work in this order unless the task explicitly narrows the scope:
1. brief intake summary of what the screen requires
2. impact summary (UI, navigation, domain, persistence, tests, docs)
3. implementation
4. verification results
5. documentation updates, if any
