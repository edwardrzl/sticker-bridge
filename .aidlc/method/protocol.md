# Stage Protocol

MANDATORY for every stage. Stage files say *what* to produce; this file says
*how* every stage talks to the person, asks questions, opens gates and records
decisions. When a stage file and this protocol disagree, the stage file wins
for its own content and this protocol wins for everything else.

`$AIDLC` below means `node .aidlc/bin/aidlc.mjs` run from the project root.

---

## 1. Language

Read `languages` from the `next` directive (sourced from `aidlc-docs/config.json`):

| Setting | Governs | Default |
|---|---|---|
| `interaction` | Everything the person reads in chat: questions, summaries, gate prompts, option labels, errors | `es` |
| `artifacts` | Every document written under `aidlc-docs/` (requirements, stories, designs, plans, questions files) | `es` |
| `code` | Identifiers, code comments, commit messages, log messages, API names | `en` |

- These instructions are in English; that never changes the language you use
  with the person. Always answer in the `interaction` language.
- Technical terms with no natural translation stay as they are (API, endpoint,
  commit, deploy, framework, backlog, CI/CD).
- Stable IDs (`FR1`, `NFR2`, `US1.3`, `U2`, `ADR-004`) are never translated.

## 2. Talking to the person (voice)

The person is a developer building *their* project. Narrate the work, never the
plumbing.

- Never say: CLI, directive, key, hook, guard, state file, audit event, gate
  machinery. Say "el workflow", "la siguiente etapa", "queda registrado".
- Between tool calls, stay quiet. No play-by-play ("ahora voy a ejecutar…").
  The person reads questions, gates and artifacts; those are the conversation.
- At the start of a stage, one short line naming the stage and what it will
  produce. That is all.
- When the CLI refuses something, do not paste its message. Say in one plain
  sentence what could not be done and why, and in one sentence what happens
  next. If the same action is refused twice, stop and ask the person.
- Errors that need the person to act get the exact command or path.

## 3. Questions (interviews)

The **questions file is the source of truth** for every decision.

### 3.1 Writing the file

Create `<artifact_dir>/<stage-slug>-questions.md` (per-unit stages: the unit's
artifact dir). Use `.aidlc/templates/questions.md` as the shape:

```markdown
## Q1 — <short topic>
<One line of context: why this matters or what depends on it.>
<The question itself, in plain words.>

A. <option>
B. <option>
C. <option>
X. Otra (especifica)

[Answer]:
```

Rules:
- Options A–E as appropriate, **always** ending with `X. Otra (especifica)`
  (in the interaction language).
- Multi-select questions say so in the text ("elige todas las que apliquen");
  answers look like `[Answer]: A, C`.
- Mark the option you recommend with `(recomendada)` and say why in one clause.
- Every question stands on its own: expand IDs ("el requisito de exportar en
  menos de 5 minutos (FR3)", never just "FR3"), define a term of art the first
  time it appears, ask in the person's domain words.
- **Never re-ask what is already answered.** Before writing questions, read
  every `*-questions.md` in the workflow directory and `aidlc-docs/memory/project.md`.
  If an earlier answer is ambiguous or contradicted by new evidence, ask a
  narrow follow-up that quotes it.
- After creating the file: `$AIDLC log --event QUESTIONS_CREATED --details "<file> — N questions"`.

### 3.2 Volume by depth

| Depth | Questions per stage | Guidance |
|---|---|---|
| minimal | ~2–4 | Only what blocks the artifact. Infer the rest and state the assumption. |
| standard | ~5–8 | Cover the stage's topic areas; follow up on vague answers. |
| comprehensive | ~8–12+ | Also edge cases, failure modes, compliance, scale, cross-cutting concerns. |

Questions shrink as the lifecycle advances: Ideation asks most, Inception
moderately, Construction only for genuine gaps, Operation only for parameters
never established.

### 3.3 Answering modes

After creating the file, ask with AskUserQuestion (see
`.claude/skills/aidlc/question-rendering.md`):

- **Guíame** — walk through the questions here, in batches.
- **Edito el archivo** — the person fills `[Answer]:` in the file and says "listo".
- **Conversemos** — free conversation; you extract the decisions.

All three converge on the file: write every answer back to its `[Answer]:` tag
as soon as you have it (chat mode adds `(modo: conversación)` after the answer).
In "Edito el archivo" mode, do not read the file until the person says they
are done.

Before ending a turn to wait for the person, every open question must exist in
the file with a blank `[Answer]:`.

### 3.4 Analyzing answers (MANDATORY)

After all answers are in:
- **Vague answers** ("depende", "no sé", "lo que tú creas", "más o menos") → follow-up.
  When the person defers to you, reframe: "Quiero que el diseño refleje *tus*
  prioridades: ¿qué te importa más, X o Y?"
- **Contradictions** — scope ("simple" + enterprise features), risk ("la
  seguridad no importa" + datos sensibles), technology (offline-first +
  colaboración en tiempo real), timeline vs scope. Show both answers side by
  side, explain the conflict, ask one targeted question. Do not proceed until
  resolved.
- **Lowering a quality target** (coverage, SLA) instead of meeting it is a red
  flag: confirm explicitly.

Append follow-ups to the same file as `## Q<n> (seguimiento)`.

### 3.5 Summary confirmation

Before generating the stage's artifacts, show a consolidated bullet summary of
the decisions (bullets, never a numbered list) and ask with AskUserQuestion:
"¿Todo correcto antes de generar los documentos?" → `Todo correcto` /
`Quiero cambiar algo`. On a change request, ask what should change, update the
answers, and summarize again. Record the confirmation:
`$AIDLC log --event ANSWERS_RECORDED --details "<file>: confirmed"`.

Skip the summary only when the stage asked no questions.

### 3.6 Assumptions stay assumptions

Anything you inferred without the person confirming it is written as
`[assumption]` in artifacts and listed under "Supuestos y preguntas abiertas".
Downstream stages never silently promote an assumption to a requirement.

## 4. Approval gates

Every stage with `gate: true` ends with a human approval. **Hard stop rule:**
after presenting the approval question you end your turn. No tool call, no
approval, no next stage until the person answers.

### 4.1 Sequence

1. Write all artifacts. Run the stage's self-check (§6).
2. If the stage names a reviewer, run it (§7).
3. `$AIDLC gate <key>` — refuses if there are no artifacts or unanswered
   questions; fix and retry.
4. Present the completion message (§5) and the approval question.
5. **End the turn.**
6. On the person's answer:
   - **Aprobar** → `$AIDLC approve <key> --choice "Aprobar"` and continue with
     the next step in the same turn.
   - **Solicitar cambios** → if the feedback is not already clear, ask what should
     change (offer concrete options drawn from the artifact). Then
     `$AIDLC reject <key> --reason "<their feedback, verbatim>"`, revise the
     artifacts (keep / modify / redo, as agreed), re-run the reviewer if one is
     named, `$AIDLC revised <key>`, and present the gate again.
   - **Otra** → discuss, then present the same gate again. Never record "Otra"
     as a decision.
   - A reply that matches no option → quote it briefly, say it did not match,
     present the same gate again.

`approve` and `reject` only succeed after a real human answer: the person's
AskUserQuestion reply or a typed message is recorded automatically. If the CLI
says no human response was recorded, you skipped the hard stop: present the
question and wait.

### 4.2 Gate options

Ideation and Inception stages:

| Label | Description |
|---|---|
| Aprobar | Continuar con `<next_after.name>` (or "Terminar el workflow" when `next_after` is null) |
| Solicitar cambios | Indicar qué revisar |
| Agregar `<etapa omitida>` | Only when a skipped stage would clearly help; use `$AIDLC scope` or ask the person how to proceed |

Construction and Operation stages: **only** Aprobar / Solicitar cambios.

After 2 revision cycles, add to the prompt: "Después de una revisión más
tendrás la opción de aceptarlo tal cual." From the 3rd revision on, add
`Aceptar tal cual` (recorded with `approve --choice "Aceptar tal cual"`).

Take `<next_after.name>` from the directive; never guess the next stage.

## 5. Completion message

In the interaction language, in this order:

1. Heading: `# <emoji> <Nombre de la etapa> completada` (plus ` — <unit>` for per-unit stages).
2. Factual summary: what was decided and produced. Include a 3–8 row table:

   | Documento | Contenido |
   |---|---|
   | requirements.md | 5 grupos funcionales (14 requisitos), 4 NFR |

   No "por favor revisa" / "avísame" filler.
3. On the first gate of a workflow, also state: profundidad (cuánto detalle
   escribo) y estrategia de pruebas (cuántas pruebas escribo), and that the
   person can ask to change either at any gate.
4. `**Revisar:** <paths to open>` — clickable paths.
5. The approval question (AskUserQuestion).

After approval, print one progress line before continuing:
`Progreso: <done>/<total> etapas · Siguiente: <next stage name>` (from `progress`
in the next directive).

## 6. Artifact rules

- **Finding inputs.** Stage files name upstream documents by file name
  (`requirements.md`). They live in the workflow directory
  (`workflow_dir` in the directive) under `<phase>/<stage>/` — or
  `construction/<unit>/<stage>/` for per-unit stages — so a Glob such as
  `<workflow_dir>/**/requirements.md` finds them. Shared knowledge lives in
  `aidlc-docs/codebase/` and `aidlc-docs/memory/`. An input that does not
  exist was skipped or is out of scope: work without it, never invent it.
- **Templates** in `.aidlc/templates/` are written in Spanish. When the
  `artifacts` language is different, translate headings and labels; keep the
  structure and the IDs.
- Write only under the directive's `artifact_dir` (and `aidlc-docs/codebase/`
  or `aidlc-docs/memory/` when a stage says so). Application code goes to the
  project tree, never under `aidlc-docs/`.
- Start every artifact with a title and a 2–4 line summary of what it is.
- Use stable IDs where the stage file defines them and carry them downstream.
- Mermaid diagrams: keep them simple (flowchart, sequenceDiagram,
  erDiagram, classDiagram). Quote labels containing punctuation. If unsure a
  diagram renders, add an equivalent ASCII or bullet version under it.
- Depth decides length: minimal = only what the next stage needs; standard =
  complete at moderate detail; comprehensive = full analysis with alternatives.
- Self-check before the gate: every section the stage requires exists, every
  upstream ID the stage must cover is covered or marked N/A with a reason, no
  placeholder text remains.

## 7. Independent review

When a stage says "Reviewer: yes", after writing artifacts dispatch the
`aidlc-reviewer` subagent with: the stage file path, the artifact paths, the
upstream artifact paths, and the depth. It writes
`<artifact_dir>/review.md` with findings `R-01…` (severity: bloqueante / mayor /
menor) and a verdict (LISTO / NO LISTO).

- NO LISTO → fix the blocking/major findings yourself and dispatch the
  reviewer again (max 2 rounds). Unresolved findings are shown at the gate.
- `$AIDLC log --event REVIEW --details "<verdict>, <n> findings"`.
- At the gate, summarize the verdict and any finding you disagreed with.

## 8. Personas

Each directive names `persona_file`. Read it and adopt that expert's voice and
priorities for the stage body. Load the persona's knowledge files only when the
stage needs them (they are listed in the persona). Personas never invoke each
other; only you delegate, and only to the subagents a stage names.

## 9. Recording

- Transitions (start, gate, approve, reject, skip, plan approval, units) are
  recorded by the CLI. Never edit `state.json`, `aidlc-state.md` or `audit.md`.
- Log what the CLI cannot see: questions created, answers confirmed, notable
  decisions made in conversation (`DECISION`), review results, memory promotion.
- Timestamps come from the CLI; never write them by hand into the audit.

## 10. Changing course

- **Depth / tests:** if the person asks, `$AIDLC depth <level> [--tests <level>]`.
- **Scope:** if the work turns out bigger/smaller, propose a different scope
  and, after the person agrees, `$AIDLC scope <name>`.
- **Going back:** an approved stage is not re-opened in place. If a later
  finding invalidates an earlier artifact, update the earlier artifact as part
  of the current stage, say so at the gate, and log a `DECISION`.
- **Pausing:** `$AIDLC park` when the person wants to stop; `/aidlc` resumes.
