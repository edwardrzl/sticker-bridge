# Tech Stack Definition

**Phase:** Inception · **Lead:** architect · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature, mvp, poc
**Inputs:** `requirements.md`, `aidlc-docs/memory/project.md`,
`aidlc-docs/codebase/technology-stack.md` (brownfield), `feasibility-assessment.md` (if any)
**Outputs:** `tech-stack-questions.md`, `tech-stack.md`, `adr/ADR-*.md`;
**promotes to** `aidlc-docs/memory/project.md` → Tech Stack

## Purpose

Decide the technology the project is built with, through an interview driven
by the requirements — not by habit. Each choice is recorded with its
rationale, and the approved stack becomes a standing rule of the project.

## Steps

### 1. Establish the baseline

- **Brownfield:** the existing stack (`technology-stack.md`) is the default.
  Ask only about *additions* the requirements need (a new library, a queue, a
  cache) and whether any existing choice must change. Changing an existing
  core choice (language, framework, database) is a big decision: flag it as
  irreversible-ish and make it its own question.
- **Greenfield:** nothing is decided unless the person already said so
  (request, intent constraints, `project.md`). Treat those as fixed.

### 2. Derive the decisions needed

From the requirements and NFRs, list which stack areas actually need a
decision for this project. Typical areas — skip the ones that do not apply:

| Area | Examples of options |
|---|---|
| Tipo de aplicación | API REST, web SSR, SPA + API, móvil, CLI, worker, librería |
| Lenguaje y runtime | TypeScript/Node, Python, Go, Java/Kotlin, C#/.NET, PHP |
| Framework backend | NestJS, Express/Fastify, FastAPI, Django, Spring Boot, ASP.NET, Laravel |
| Frontend | React/Next.js, Vue/Nuxt, Angular, Svelte, server-rendered templates |
| Base de datos | PostgreSQL, MySQL, SQLite, MongoDB, DynamoDB |
| Acceso a datos | ORM (Prisma, TypeORM, SQLAlchemy, EF Core) vs query builder vs SQL |
| Autenticación | sesión propia, JWT, OAuth/OIDC con proveedor (Auth0, Cognito, Keycloak, Clerk) |
| Mensajería / jobs | ninguna, cola (SQS, RabbitMQ), jobs programados |
| Pruebas | runner unitario, integración, E2E (Jest/Vitest, Pytest, JUnit, Playwright) |
| Calidad | formatter, linter, type checking |
| Empaquetado y despliegue | contenedor, serverless, PaaS, VPS; proveedor cloud |
| Observabilidad | logging estructurado, métricas, tracing |

### 3. Interview

Create `tech-stack-questions.md`. For each needed decision, one question with
2–4 realistic options suited to *this* project. Each option's text states its
main trade-off in one clause; mark your recommendation with a one-line reason
tied to a requirement, NFR, constraint or the team's skills ("recomendada:
cumple NFR2 de latencia y el equipo ya conoce TypeScript").

Always ask, early:
- The team's familiarity: which languages/frameworks the person (or team) is
  comfortable maintaining. Skills weigh heavily.
- Hosting constraints and budget, if not known yet.

Then check the answer set for incompatibilities (e.g. serverless + long-lived
WebSockets, SQLite + multi-instance writes, a framework's version vs the
runtime chosen) and resolve them before writing.

### 4. Write `tech-stack.md` and ADRs

```markdown
# Stack tecnológico

## Resumen
| Área | Elección | Versión | Motivo (req./restricción) |
| Lenguaje | TypeScript | 5.x | equipo, NFR… |

## Estructura del proyecto
<Top-level layout the code will follow (monorepo or not, folders).>

## Comandos estándar
| Acción | Comando |
| Instalar | |
| Ejecutar en local | |
| Pruebas | |
| Lint / formato | |
| Build | |

## Alternativas descartadas
## Riesgos y decisiones irreversibles
## Supuestos y preguntas abiertas
```

Pin major versions to current stable releases you are confident about; if
unsure of the latest version, write the major version and mark it
`[verificar versión]`.

For each significant or hard-to-reverse choice (language, framework, database,
auth approach, hosting), write `adr/ADR-00N-<slug>.md` using
`.aidlc/knowledge/architecture/adr-template.md` (context, decision,
consequences, alternatives).

### 5. Gate, then promote

Emoji: 🧱. Summary: the stack table and the ADR list. Review: `tech-stack.md`,
`adr/`.

**After approval**: replace the content under `## Stack tecnológico (Tech Stack)`
in `aidlc-docs/memory/project.md` with the summary table plus the standard
commands, and a link to this workflow's `tech-stack.md`. Then
`$AIDLC log --event MEMORY_PROMOTED --details "stack → project.md"`.
