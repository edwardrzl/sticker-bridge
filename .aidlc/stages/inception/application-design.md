# Application Design

**Phase:** Inception · **Lead:** architect · **Gate:** yes · **Reviewer:** yes (standard and comprehensive depth)
**Scopes:** enterprise, feature, mvp
**Inputs:** `requirements.md`, `stories.md` + `personas.md` (if produced), `tech-stack.md`,
`aidlc-docs/codebase/architecture.md` (brownfield), `aidlc-docs/memory/project.md`
**Outputs:** `application-design-questions.md`, `components.md`, `domain-model.md`,
`interfaces.md`, `adr/ADR-*.md`

## Purpose

Decide the building blocks of the system — components, their
responsibilities, the domain model and how the pieces talk — so units can be
carved out and built in parallel without surprises.

## Steps

### 1. Load context

Read the inputs. Brownfield: the design extends the existing architecture;
new components must fit its style unless the person decides otherwise.

### 2. Interview (design decisions only)

Create `application-design-questions.md` for real decisions, for example:
architectural style (layered, hexagonal, modular monolith, services), how
modules communicate (in-process calls, REST, events), where business rules
live, multi-tenancy, sync vs async for slow work, API style (REST,
GraphQL, RPC), state management on the frontend. When more than one
decomposition is viable, present 2–3 options with trade-offs as a question.

### 3. Write the design

`components.md`:
- A table of components: ID `CMP-0n`, name, responsibility, owned entities,
  exposes, depends on.
- A Mermaid component/flow diagram.
- Rationale: why these boundaries.

`domain-model.md`: entities and value objects with key attributes, their
owning component, relationships (Mermaid `erDiagram` or `classDiagram`),
invariants and business rules (`BR-0n`) linked to FRs.

`interfaces.md`: for each component boundary, the operations (endpoint /
function / event), inputs, outputs, errors, and which stories use them. Not
full OpenAPI — enough for Functional Design to detail per unit.

ADRs for significant choices (`adr/ADR-00N-*.md`, numbering continues from
Tech Stack's ADRs if any).

Traceability: a table FR/US → components. Every FR is owned by at least one
component.

### 4. Review

Dispatch `aidlc-reviewer` with the four documents and upstream
`requirements.md`, `stories.md`, `tech-stack.md`
(knowledge for the reviewer: `.aidlc/knowledge/reviewing/architecture-review.md`).

### 5. Gate

Emoji: 🏗️. Summary: style, component count and one line each, key ADRs,
coverage. Review: `components.md`, `domain-model.md`, `interfaces.md`.
