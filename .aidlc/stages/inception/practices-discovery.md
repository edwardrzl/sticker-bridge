# Practices Discovery

**Phase:** Inception · **Lead:** delivery · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature, mvp
**Inputs:** `aidlc-docs/memory/project.md`, `workspace-analysis.md`,
`aidlc-docs/codebase/*` (brownfield), git history and CI config
**Outputs:** `practices-discovery-questions.md`, `team-practices.md`;
**promotes to** `aidlc-docs/memory/project.md`

## Purpose

Make explicit *how* this project is built: way of working, walking skeleton,
testing posture, deployment, code style, and hard rules. Once affirmed, these
become standing rules in `project.md` that every later session follows.

## Steps

### 1. Gather evidence

- If `project.md` already has these sections filled, they are the baseline:
  this stage only confirms or updates them. At minimal depth with a complete
  baseline, ask a single question: "¿Sigue vigente todo esto?" and show it.
- Brownfield: infer from evidence before asking — `git log --oneline -30`
  (commit style), branches, CI files, lint/format config, test layout,
  `aidlc-docs/codebase/code-quality.md`.
- Greenfield: no evidence; propose sensible defaults for the likely stack.

### 2. Interview

Create `practices-discovery-questions.md`. Ask only what evidence did not
settle. Topic areas, in the person's words (never the framework's section names):

- **Forma de trabajo** — branching (trunk-based / feature branches /
  GitFlow), who reviews, commit message convention, whether the AI should
  commit and when, definition of done.
- **Esqueleto funcional** — ask it as: "¿Construimos primero una rebanada
  mínima de punta a punta? Un esqueleto funcional es una versión mínima que
  recorre todo el sistema, hecha primero para probar que las piezas conectan
  antes de meter las funcionalidades reales."
- **Postura de pruebas** — TDD (test first), test-after, BDD, or mixed;
  coverage target; which test types matter (unit/integration/E2E).
- **Despliegue** — where it runs (local only, VPS, cloud provider, container
  platform), environments (dev/staging/prod), cadence.
- **Estilo de código** — formatter and linter, naming, folder structure
  (by layer / by feature), error-handling style.
- **Reglas duras** — anything that must always or never happen ("siempre
  usar migraciones", "nunca SQL crudo", "nunca subir .env").

### 3. Write `team-practices.md`

Sections exactly matching `project.md`: Forma de trabajo, Esqueleto
funcional, Postura de pruebas, Despliegue, Estilo de código, Reglas
obligatorias (`- SIEMPRE …`), Reglas prohibidas (`- NUNCA …`). Postura de
pruebas must contain:
- `- **Metodología**: tdd | bdd | atdd | test-after | mixta`
- `- **Orden**: <one explicit sentence of what is written first>`

Only the person's stated hard constraints go into the rules sections.

### 4. Gate, then promote

Emoji: 🧭. Summary: the practices in a compact table. Review: `team-practices.md`.

**After approval** (and only then): update `aidlc-docs/memory/project.md` —
replace the content under each matching heading (Way of Working, Walking
Skeleton, Testing Posture, Deployment, Code Style, Mandated, Forbidden) with
the affirmed content. Keep other sections untouched. Then
`$AIDLC log --event MEMORY_PROMOTED --details "practices → project.md"`.
