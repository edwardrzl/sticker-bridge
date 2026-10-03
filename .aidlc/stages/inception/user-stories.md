# User Stories

**Phase:** Inception · **Lead:** product · **Gate:** yes · **Reviewer:** yes (comprehensive depth)
**Scopes:** enterprise, feature, mvp — **conditional**
**Inputs:** `requirements.md`, `intent-statement.md`, `scope-document.md`
**Outputs:** `user-stories-questions.md`, `personas.md`, `stories.md`

## Purpose

Express the requirements from the users' point of view, with acceptance
criteria concrete enough to test and to plan code against.

## Condition

Run when the system has human users with distinct goals (UI, multi-role
API products, admin workflows). Skip — `$AIDLC skip user-stories --reason "…"`
— when requirements are purely technical (internal library, data pipeline, CLI
for one developer, infrastructure change) and FR acceptance criteria already
cover the behavior. When unsure, ask the person with one AskUserQuestion.

## Steps

### 1. Plan

Decide the breakdown approach and ask about it if not obvious:
by user journey, by feature area, by persona, or by domain entity. Also ask
the granularity (epics + stories, or flat stories) and the format
(Given/When/Then or checklist criteria). Keep it to the questions that matter
at the current depth.

### 2. Write `personas.md`

For each persona: name/role, goals, frustrations, technical comfort, which
capabilities they use. Two to five personas; no invented demographics.

### 3. Write `stories.md`

```markdown
# Historias de usuario

## Épica E1 — <nombre>
### US1.1 — <título>
**Como** <persona> **quiero** <acción> **para** <beneficio>.
- **Requisitos:** FR1.1, FR1.2
- **Prioridad:** Must | Should | Could
- **Tamaño:** S | M | L
**Criterios de aceptación**
- Dado … cuando … entonces …
- Dado … (caso de error) …

## Cobertura
| Requisito | Historias |
| FR1.1 | US1.1 |
```

Rules: INVEST (independent, negotiable, valuable, estimable, small, testable);
every FR maps to at least one story or is marked N/A with a reason; error
and edge cases appear as acceptance criteria; stable IDs `US{epic}.{n}`.

### 4. Review (comprehensive depth)

Dispatch `aidlc-reviewer` with `stories.md`, `personas.md`, upstream
`requirements.md`.

### 5. Gate

Emoji: 📖. Summary: personas, epics, story count by priority, coverage of
FRs. Review: `stories.md`, `personas.md`.
