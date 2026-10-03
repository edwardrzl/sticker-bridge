# Code Generation

**Phase:** Construction (per unit) · **Lead:** developer · **Gate:** yes (plus plan approval) · **Reviewer:** yes (standard and comprehensive depth)
**Scopes:** all
**Inputs:** everything designed for this unit (`functional-design.md`, `nfr-requirements.md`,
`nfr-design.md`, `infrastructure-design.md` — those that exist), `unit-of-work.md`,
`unit-story-map.md`, `interfaces.md`, `requirements.md`, `tech-stack.md`,
`aidlc-docs/memory/project.md`, `aidlc-docs/codebase/*` (brownfield).
Without units (poc, bugfix, refactor): `requirements.md`, `tech-stack.md` (if any), codebase docs.
**Outputs:** artifact dir: `code-generation-plan.md`, `unit-test-instructions.md`,
`code-summary.md` (+ `code-generation-progress.md` under strict guard);
application code and tests in the project tree.

Read `.aidlc/method/construction.md` first if you have not this session.

## Part 1 — Plan

### 1. Read the unit's artifacts

Read all inputs that exist. Never invent the content of a missing artifact;
scopes without design stages plan from requirements and the codebase.

### 2. Brownfield baseline

Existing code: run the current test suite and record the baseline; compute
the blast radius (construction.md §6). Read the files you will change.

### 3. Write the plan

`code-generation-plan.md` from `.aidlc/templates/code-generation-plan.md`:
- Numbered steps with checkboxes, each tied to stories/FRs, with files and a
  verifiable "hecho cuando".
- Ordered per the testing posture (TDD: Red → Green → Refactor per layer;
  test-after: layer then its tests). The first step makes the test runner work
  (bootstrap on greenfield, verify on brownfield).
- Test files are mandatory steps, sized by the test strategy and scope floors
  (construction.md §5).
- Walking-skeleton unit: the plan builds the thinnest runnable end-to-end path
  first.
- Configuration, `.env.example`, README/docs updates, and migrations as steps.

`unit-test-instructions.md`: framework and config, the exact command to run
**this unit's** tests (scoped by path or filter, not the whole suite),
coverage target, mocking guidance, test data.

### 4. Plan approval (checkpoint)

`$AIDLC plan-request <key> --plan <artifact_dir>/code-generation-plan.md`, then
present a summary (steps, files to create/modify, tests planned, risks) and
ask: `Aprobar plan` / `Solicitar cambios`. **End the turn.** Then
`plan-approve` or `plan-reject` + revise + `plan-request` again
(construction.md §3).

## Part 2 — Build

### 5. Implement the plan

Step by step, in order:
- Write code and tests exactly as planned, in the project tree.
- Run the unit's tests after each step that adds tests or behavior; a step is
  done when its tests pass.
- Tick the step in the plan (strict guard: in `code-generation-progress.md`).
- If a step turns out wrong or incomplete, stop, update the plan, and get it
  approved again before continuing.
- Follow construction.md §4 code rules (in-place edits, conventions, never
  lower thresholds, no secrets).

### 6. Verify

Run the unit's test command and, when configured, lint/format/type checks.
Brownfield: run the full suite and compare with the baseline; fix
regressions.

### 7. Write `code-summary.md`

```markdown
# Resumen de código — <unidad>
## Archivos
| Archivo | Acción (creado/modificado/eliminado) | Propósito |
## Cómo ejecutar
## Pruebas
- Comando: `…` · Resultado: N pasan, 0 fallan · Cobertura: …%
## Desviaciones del plan
## Pendientes y deuda conocida
## Trazabilidad
| Historia/FR | Archivos | Pruebas |
```

### 8. Review

Standard/comprehensive depth: dispatch `aidlc-reviewer` with the plan,
`code-summary.md`, the changed source files list, and upstream design
documents. Its focus: plan fidelity, test adequacy, security issues,
convention violations.

### 9. Gate

Emoji: 💻 — heading `Código generado — <unit>`. Summary: files created and
modified, tests and results, coverage, deviations. Review: `code-summary.md`
and the main source paths. Options: Aprobar / Solicitar cambios.

If the person asked for commits (project.md → Way of Working), commit after
approval, as construction.md §8 says.
