# Requirements Analysis

**Phase:** Inception · **Lead:** product · **Gate:** yes · **Reviewer:** yes (standard and comprehensive depth)
**Scopes:** all
**Inputs:** `request.md`, `intent-statement.md`, `scope-document.md`,
`re-summary.md` + `aidlc-docs/codebase/*` (brownfield), `aidlc-docs/memory/project.md`
**Outputs:** `requirements-analysis-questions.md`, `requirements.md`

## Purpose

State precisely and testably what the system must do (functional) and how
well (non-functional). For a bugfix, what "fixed" means; for a refactor, what
behavior must stay identical.

## Steps

### 1. Load and analyze

Read the inputs. Classify the request: clarity (clear / vague), type (new
product, feature, enhancement, bugfix, refactor, migration), breadth (one
component / several / system-wide), complexity (simple / standard / complex).

### 2. Completeness analysis

For what you know so far, list gaps across: functional behavior, user roles
and permissions, data (what is stored, retained, imported/exported), business
rules and validations, error cases, integrations, NFRs (performance,
availability, security, privacy, accessibility, localization), constraints,
acceptance.

By scope:
- **bugfix** — expected vs actual behavior, reproduction steps, affected
  versions/environments, severity, what must not change.
- **refactor** — goal (readability, performance, modularity, upgrade),
  behavior that must be preserved, areas in/out, success criteria.
- **poc** — the hypothesis to prove and what "proven" looks like; everything
  else minimal.

### 3. Interview

Create `requirements-analysis-questions.md` for the gaps (protocol §3 for
volume, modes, analysis, contradictions and summary confirmation). Never ask
what the intent or scope documents already answered.

### 4. Write `requirements.md`

```markdown
# Requisitos

## Resumen de la intención
<What the person is trying to achieve — goals, not just features.>

## Requisitos funcionales
### FR1 — <área>
- **FR1.1** <requirement> — *Criterio de aceptación:* <verifiable>
- **FR1.2** …

## Requisitos no funcionales
| ID | Categoría | Requisito | Meta medible | Cómo se verifica |
| NFR1 | Rendimiento | | p95 < 300 ms | prueba de carga |

## Restricciones
## Supuestos
## Fuera del alcance
## Preguntas abiertas
```

IDs are stable: `FR{n}`, `FR{n}.{m}`, `NFR{n}`. Every capability `CAP-xx` from
the scope document maps to at least one FR (add a small mapping table when
the scope document exists). Bugfix: include "Comportamiento esperado",
"Comportamiento actual", "Pasos para reproducir". Refactor: include
"Comportamiento que se preserva".

### 5. Review

Standard/comprehensive depth: dispatch `aidlc-reviewer` (protocol §7) with
`requirements.md`, and as upstream `intent-statement.md`, `scope-document.md`
and `re-summary.md` where they exist.

### 6. Gate

Emoji: 📋. Summary table: FR groups and counts, NFR count, key constraints,
open questions. First gate of the workflow: include depth and test strategy
(protocol §5). Review: `requirements.md`.
