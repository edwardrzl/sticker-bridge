# Construction Protocol

Load this file on the first Construction stage of a session, together with
`protocol.md`. It covers what is specific to building: units, bolts, plan
approval, tests, and brownfield safety.

`$AIDLC` means `node .aidlc/bin/aidlc.mjs`.

## 1. Units and iteration

- When the scope includes Units Generation, construction runs **per unit**,
  unit by unit (unit-major): for each unit in order, its design stages and then
  its Code Generation; Build and Test runs once after every unit is built.
- Per-unit stage keys look like `code-generation@u1-catalog`. Artifacts live in
  `aidlc-docs/workflows/<id>/construction/<unit>/<stage>/`.
- Without units (poc, bugfix, refactor), each construction stage runs once for
  the whole change.
- On re-entry for another unit, say one line naming the unit being built.
  Nothing about iteration mechanics.
- A per-unit design stage that genuinely does not apply to a unit (e.g.
  Infrastructure Design for a pure library) is skipped with
  `$AIDLC skip <key> --reason "<specific reason>"` — never by writing empty
  documents.

## 2. Walking skeleton

When the team practices (`aidlc-docs/memory/project.md` → Walking skeleton) say
yes, or the scope is mvp/feature/enterprise on a greenfield project and the
practices say nothing: the first unit is built as the thinnest end-to-end slice
that runs (entry point → logic → persistence → response, with one real test),
proving the pieces connect before real features go in. Say so in its plan.

## 3. Code Generation plan approval

Code Generation has two human checkpoints: the **plan** and the **final gate**.

1. Write `code-generation-plan.md` (template: `.aidlc/templates/code-generation-plan.md`)
   and `unit-test-instructions.md` in the artifact dir.
2. `$AIDLC plan-request <key> --plan <artifact_dir>/code-generation-plan.md`
3. Present the plan summary and ask with AskUserQuestion:
   `Aprobar plan` / `Solicitar cambios`. **End the turn.**
4. On approval: `$AIDLC plan-approve <key> --choice "Aprobar plan"`.
   On changes: `$AIDLC plan-reject <key> --reason "<feedback>"`, revise, and go
   back to step 2.
5. Only now can application code be written — the project's guard refuses code
   edits before this point, and (in strict mode) after any edit to the approved
   plan. If the plan must change mid-implementation, update it and present it
   again (steps 2–4).

Mark plan checkboxes `[x]` as you complete each step. The plan file is the
progress record; keep it truthful. Ticking checkboxes after approval does
change the plan's content: under `guard: strict` keep a separate
`code-generation-progress.md` with the checklist copy and leave the approved
plan untouched.

## 4. Code rules

- Application code goes to the project tree, **never** under `aidlc-docs/`.
- Follow `aidlc-docs/memory/project.md`: stack, versions, conventions, code
  style, mandated/forbidden rules. Those are the person's decisions; do not
  swap a library or pattern without asking.
- Identifiers, comments and commit messages in the `code` language.
- Brownfield: modify files in place. Never create `Foo_new.ts`,
  `Foo_modified.java` or parallel copies.
- UI: add `data-testid` to interactive elements.
- Measured targets from NFRs and the test strategy (coverage floor,
  latency budget) are obligations. **Never lower, relax or disable a
  threshold** (coverage config, lint rules, skipped tests) to make something
  pass. Surface the gap instead.
- No secrets in code. Configuration via environment variables with a
  documented `.env.example`.

## 5. Tests

Tests are written during Code Generation, not deferred to Build and Test.

| Strategy | Unit tests | Integration | E2E |
|---|---|---|---|
| minimal | ≥1 per requirement touched, happy path per component | only when a bug/boundary needs it | — |
| standard | 5–8 per component | stubs for key boundaries | — |
| comprehensive | 10–15 per component | per boundary | critical user journeys |

Scope floors on top of the strategy:
- `mvp`, `feature`, `enterprise`: 80% line coverage on new code and tests run in CI.
- `bugfix`: a regression test that fails before the fix and passes after, at the
  narrowest level that reproduces the bug.
- `refactor`: the existing suite stays green before and after; add
  characterization tests where coverage is missing around the refactored code.

Testing posture (TDD / test-after / BDD…) comes from `project.md`. With TDD,
the plan orders each layer as Red → Green → Refactor; with test-after, layer
then its tests. Either way the test runner must work before the first test
step (bootstrap it on greenfield; verify it on brownfield) and
`unit-test-instructions.md` records the exact command scoped to this unit.

## 6. Brownfield safeguards

Before changing existing code:
1. **Test baseline** — run the existing suite and record totals (passing,
   failing, skipped, coverage) in the plan. Pre-existing failures are noted,
   not fixed silently.
2. **Blast radius** — list the files to change, their consumers and tests;
   classify low / medium / high; show it in the plan.
3. **Diff discipline** — minimal, focused changes; no drive-by reformatting.

After changing: re-run the suite. New failures are regressions you introduced;
fix them before the gate.

## 7. Build and Test failures

If Build and Test finds failures in code of an already-approved unit, fix them
within Build and Test (it may write code) and list every fix at its gate. If
the fix needs a design change, stop and ask the person before changing course.

## 8. Commits

Do not commit unless the person asked for commits in their practices
(`project.md` → Way of working) or asks now. When committing: one commit per
unit (or per plan step if practices say so), conventional message in the
`code` language, never skipping hooks.
