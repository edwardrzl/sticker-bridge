# NFR Requirements

**Phase:** Construction (per unit) · **Lead:** devsecops · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature
**Inputs:** `requirements.md` (NFR section), the unit's `functional-design.md`,
`tech-stack.md`, `interfaces.md`, `aidlc-docs/memory/project.md`
**Outputs (unit artifact dir):** `nfr-requirements-questions.md` (if gaps), `nfr-requirements.md`

## Purpose

Turn the project-level NFRs into measurable targets for this unit, add the
unit-specific ones, and threat-model its boundaries.

## Steps

### 1. Assess categories

For this unit: performance (latency, throughput), security (authn, authz,
data protection, input validation, secrets), scalability (expected load,
growth), reliability (availability, failure handling, backup/recovery),
observability (logs, metrics, traces, alerts), plus privacy/compliance when
personal or regulated data is involved.

### 2. Questions only for missing numbers

If a target is vague ("rápido", "seguro", "alta disponibilidad") or missing
and matters for this unit, ask for a number or choose between concrete
options. Construction asks little: skip what the project NFRs already fix.

### 3. Write `nfr-requirements.md`

```markdown
# Requisitos no funcionales — <unidad>

| ID | Hereda de | Categoría | Requisito | Meta | Verificación |
| NFR1.1 | NFR1 | Rendimiento | | p95 < 300 ms @ 50 rps | prueba de carga k6 |

## Modelo de amenazas (STRIDE)
| Amenaza | Activo / frontera | Mitigación | Estado |

## Decisiones técnicas derivadas
<Libraries or settings the targets require (rate limiter, password hashing
algorithm, connection pool size…) — consistent with tech-stack.md; anything
new is flagged for the person.>

## Cobertura de NFR del proyecto
| NFR | Estado (OK / N/A + motivo) | Requisitos de la unidad |
```

IDs inherit the project NFR (`NFR4` → `NFR4.1`, `NFR4.2`). STRIDE depth
follows the depth setting: minimal = the top 3 threats, comprehensive = full
table per boundary.

### 4. Gate

Emoji: 🛡️. Summary: targets by category, main threats and mitigations.
Review: `nfr-requirements.md`. Options: Aprobar / Solicitar cambios.
