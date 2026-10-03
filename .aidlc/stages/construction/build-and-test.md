# Build and Test

**Phase:** Construction · **Lead:** quality · **Gate:** yes · **Reviewer:** no
**Scopes:** all
**Inputs:** every unit's `code-summary.md` and `unit-test-instructions.md`, `requirements.md`,
`stories.md` (if any), NFR documents, `tech-stack.md`, `aidlc-docs/memory/project.md`
**Outputs:** `build-and-test-summary.md`, `test-instructions.md`; fixes and missing tests in the project tree

## Purpose

Prove the whole change works together: clean build, all tests green, the
requirements covered, and the instructions a human needs to reproduce it.

## Steps

### 1. Build from clean

Install dependencies and build with the project's standard commands
(`project.md` / `tech-stack.md`). Record exact commands and results.

### 2. Run every test layer

- Each unit's scoped test command (from `unit-test-instructions.md`).
- The full suite, once.
- Integration tests across units (add them now if units interact and none
  exist — the test strategy says how many).
- E2E tests when the strategy is comprehensive or the scope floor requires
  them.
- Lint, format check and type check if configured.
- NFR checks that are cheap to run locally (a quick load test script,
  security linters, dependency audit), otherwise document how to run them.

### 3. Fix failures

Fix failing builds or tests (this stage may write code). Classify each
failure: regression introduced by this workflow, pre-existing (brownfield
baseline) or environment. Never delete, skip or weaken a test, and never
lower a threshold, to get green; if a fix needs a design change, stop and ask
the person.

### 4. Coverage against requirements

Table: each FR / US → tests that cover it → status. Gaps get a test now or an
explicit reason. Report line coverage against the scope floor (80% on new code
for mvp/feature/enterprise).

### 5. Write the documents

`test-instructions.md` — how a human builds and tests the project from a fresh
clone: prerequisites, env vars, commands per test layer, how to run locally.

`build-and-test-summary.md`:
```markdown
# Resumen de build y pruebas
## Build
| Paso | Comando | Resultado |
## Pruebas
| Capa | Comando | Pasan | Fallan | Omitidas | Cobertura |
## Correcciones realizadas en esta etapa
## Cobertura de requisitos
| FR/US | Pruebas | Estado |
## Fallas preexistentes (no introducidas por este workflow)
## Riesgos y pendientes
```

Every number comes from an actual run in this stage.

### 6. Gate

Emoji: ✅. Summary: build status, test counts per layer, coverage, fixes
made. Review: `build-and-test-summary.md`, `test-instructions.md`. Options:
Aprobar / Solicitar cambios.
