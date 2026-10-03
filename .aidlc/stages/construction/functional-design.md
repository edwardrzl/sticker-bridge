# Functional Design

**Phase:** Construction (per unit) · **Lead:** architect · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature, mvp
**Inputs:** the unit's entry in `unit-of-work.md`, its stories (`unit-story-map.md`,
`stories.md`), `components.md`, `domain-model.md`, `interfaces.md`, `requirements.md`,
`tech-stack.md`, `aidlc-docs/memory/project.md`
**Outputs (unit artifact dir):** `functional-design-questions.md` (only if gaps), `functional-design.md`

## Purpose

Detail exactly what this unit does — business logic, data, interfaces and
error behavior — so Code Generation can plan against a precise spec.

## Steps

### 1. Load the unit's slice

Read only what concerns this unit: its components, entities, interfaces and
stories. Earlier units' `functional-design.md` are inputs when this unit
depends on them (use their interfaces, do not redefine them).

### 2. Questions only for genuine gaps

Construction asks little. Create a questions file only for gaps prior stages
did not settle (a business rule edge case, a validation limit, a state
transition). Minimal depth: none unless blocking.

### 3. Write `functional-design.md`

Adapt sections to the unit kind (a `library` has no endpoints; a `ui` has
screens instead of tables):

```markdown
# Diseño funcional — <U1 — nombre>

## Alcance de la unidad
Historias: US… · Requisitos: FR…

## Modelo de datos
<Entities/tables with fields, types, constraints, indexes; Mermaid erDiagram.
Migrations needed.>

## Lógica de negocio
<Per operation: preconditions, steps, rules BR-xx applied, postconditions.
State machines as Mermaid stateDiagram when entities have lifecycles.>

## Interfaces
<Endpoints (method, path, request, response, status codes) / public functions /
events / screens and their interactions. Consistent error format.>

## Validaciones y errores
| Caso | Condición | Respuesta |

## Flujos principales
<Mermaid sequenceDiagram for the 1–3 most important flows.>

## Casos de prueba derivados
<Scenarios, from acceptance criteria, that Code Generation must test.>

## Trazabilidad
| Historia/FR | Sección |
```

### 4. Gate

Emoji: 📝 — heading `Diseño funcional completado — <unit>`. Summary:
entities, operations/endpoints, rules, test scenarios count. Review:
`functional-design.md`. Gate options: Aprobar / Solicitar cambios only.
