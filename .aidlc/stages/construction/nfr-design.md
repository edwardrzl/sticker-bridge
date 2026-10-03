# NFR Design

**Phase:** Construction (per unit) · **Lead:** architect · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature
**Inputs:** the unit's `nfr-requirements.md` and `functional-design.md`, `tech-stack.md`,
`components.md`, `aidlc-docs/memory/project.md`
**Outputs (unit artifact dir):** `nfr-design.md`

## Purpose

Decide *how* the unit meets its non-functional targets: the patterns and
concrete mechanisms Code Generation will implement.

## Steps

### 1. Map targets to patterns

For each NFR requirement of the unit, choose the mechanism
(`.aidlc/knowledge/architecture/nfr-design-patterns.md`): caching strategy,
pagination, indexes, connection pooling, timeouts/retries/circuit breakers,
idempotency keys, rate limiting, authn/authz enforcement point, encryption
at rest/in transit, structured logging fields, metrics and traces, health
checks, graceful degradation.

Prefer the simplest mechanism that meets the target. If two reasonable
options differ in cost or complexity, ask the person (one question each).

### 2. Write `nfr-design.md`

```markdown
# Diseño NFR — <unidad>

| Requisito | Mecanismo | Dónde se implementa | Cómo se verifica |
| NFR1.1 | caché de lectura 60 s | servicio de catálogo | prueba de carga |

## Seguridad
<Enforcement points, authz rules per operation, validation strategy, secrets handling.>

## Resiliencia
<Timeouts, retries, fallbacks per external dependency.>

## Observabilidad
<Log format and fields, metrics names, trace spans, health endpoint.>

## Configuración
<Environment variables introduced, with defaults and which are secrets.>

## Impacto en el plan de código
<What Code Generation must add: middleware, config, tests.>
```

### 3. Gate

Emoji: ⚙️. Summary: mechanisms per category and what they add to the code.
Review: `nfr-design.md`. Options: Aprobar / Solicitar cambios.
