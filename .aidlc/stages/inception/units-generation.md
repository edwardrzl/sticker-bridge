# Units Generation

**Phase:** Inception · **Lead:** architect · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature, mvp
**Inputs:** `components.md`, `domain-model.md`, `interfaces.md`, ADRs,
`requirements.md`, `stories.md` (if produced), `aidlc-docs/memory/project.md`
**Outputs:** `units-generation-questions.md`, `unit-of-work.md`, `unit-story-map.md`

## Purpose

Group the components into units of work — pieces that can be designed, built
and tested on their own — and state their dependencies. Construction runs
unit by unit in an order that respects these dependencies.

This stage describes topology only (what depends on what). Which unit to
build first for value or risk is Delivery Planning's decision.

## Steps

### 1. Interview

Create `units-generation-questions.md` (keep it short; many answers follow
from the design):
- Boundary strategy: by component, by feature slice, by deployable, by layer
  (discourage pure layers — they cannot run on their own).
- Granularity: few coarse units vs many fine ones. Guideline: a unit is
  buildable in one bolt (hours to a few days).
- Deployment model: single deployable, several, hybrid.

If the walking skeleton applies (project.md, or mvp/feature/enterprise on
greenfield), the first unit must be the thinnest end-to-end slice that runs.

### 2. Write `unit-of-work.md`

Use `.aidlc/templates/unit-of-work.md`. Per unit: ID `U{n}`, directory id
`u{n}-<kebab-name>`, name, kind (service / ui / library / spec / packaging),
responsibility, components included, stories/FRs, deployment, complexity
(S/M/L/XL), implementation notes. Mermaid dependency diagram. Integration
points between units.

The fenced ` ```json ` block with `{"units": [...]}` is mandatory: ids are the
directory ids (`u1-catalog`), `depends_on` lists directory ids, no cycles.

### 3. Write `unit-story-map.md`

Table: story (or FR when there are no stories) → unit. Every story/FR is
assigned to exactly one implementing unit; cross-cutting ones are noted.

### 4. Register the units

`$AIDLC units set --file <artifact_dir>/unit-of-work.md` — the CLI validates
ids, dependencies and cycles. Fix the document and retry if it refuses.

### 5. Gate

Emoji: 🧩. Summary: units with kind and size, dependency order, story
coverage. Review: `unit-of-work.md`, `unit-story-map.md`.
