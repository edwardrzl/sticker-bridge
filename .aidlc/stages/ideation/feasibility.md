# Feasibility

**Phase:** Ideation · **Lead:** architect · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature
**Inputs:** `intent-statement.md`, `workspace-analysis.md`, `aidlc-docs/codebase/` (if any)
**Outputs:** `feasibility-questions.md` (if needed), `feasibility-assessment.md`

## Purpose

Check, before investing in detailed specs, that the intent can be delivered
within its constraints — technically, in time, with the team and budget
available — and surface the risks that will shape the design.

## Steps

### 1. Assess

From the intent and what exists, evaluate:
- **Technical** — unknowns, integrations, data availability, performance
  or scale that is hard to reach, third-party dependencies and their limits.
- **Delivery** — size vs deadline, team skills vs likely stack.
- **Cost** — order-of-magnitude running and build costs where relevant.
- **Risk** — security, compliance, vendor lock-in, data migration.

### 2. Interview (only for gaps)

If feasibility hinges on facts you do not have (expected volumes, existing
systems to integrate, budget ceiling, mandated platform), create
`feasibility-questions.md` and ask. Minimal depth: at most 3 questions.

### 3. Write `feasibility-assessment.md`

```markdown
# Evaluación de factibilidad

## Veredicto
Factible | Factible con condiciones | No factible tal como está — <one line>

## Evaluación
| Dimensión | Nivel (bajo/medio/alto riesgo) | Justificación |
| Técnica | | |
| Entrega (tiempo/equipo) | | |
| Costo | | |
| Seguridad / cumplimiento | | |

## Riesgos principales
| ID | Riesgo | Probabilidad | Impacto | Mitigación |
| RSK-01 | | | | |

## Restricciones confirmadas
## Condiciones para avanzar
## Supuestos y preguntas abiertas
```

If the verdict is "No factible tal como está", say so plainly at the gate and
propose how to reshape the intent (smaller scope, POC first, different
constraint).

### 4. Gate

Emoji: 🔍. Summary: verdict, top 3 risks, conditions. Review:
`feasibility-assessment.md`.
