# Delivery Planning

**Phase:** Inception · **Lead:** delivery · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature
**Inputs:** `unit-of-work.md`, `unit-story-map.md`, `backlog.md`, `feasibility-assessment.md`,
`aidlc-docs/memory/project.md`
**Outputs:** `delivery-planning-questions.md`, `bolt-plan.md`

## Purpose

Choose the order in which units are built (bolts) — by value and risk,
within what the dependencies allow — and define what each bolt must
demonstrate when it finishes.

## Steps

### 1. Interview

Create `delivery-planning-questions.md`:
- Sequencing priority: risk first (prove the hardest part early) vs value
  first (ship the most useful slice early) vs dependency order only.
- Demo expectations: what the person wants to see running after each bolt.
- Milestones or dates, if any.
- Checkpoints: review every bolt (default) or group small bolts.

### 2. Write `bolt-plan.md`

```markdown
# Plan de entrega

## Estrategia
<risk-first | value-first | dependency order — and why>

## Bolts
| # | Unidad | Objetivo | Demostración al terminar | Riesgos que reduce |
| 1 | U1 — … | esqueleto funcional | `curl …` devuelve … | integración BD |

## Hitos
## Dependencias externas y bloqueos
```

The order must respect `depends_on`. Only units registered in Units
Generation appear.

### 3. Register the order

`$AIDLC units order <u1-id,u2-id,…>` — the CLI refuses an order that breaks a
dependency; fix and retry.

### 4. Gate

Emoji: 🗺️. Summary: strategy, bolt order with one-line goals. Review:
`bolt-plan.md`. Say that the next step is Construction, starting with the
first unit.
