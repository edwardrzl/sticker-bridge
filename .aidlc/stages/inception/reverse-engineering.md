# Reverse Engineering

**Phase:** Inception · **Lead:** architect (synthesis) + `aidlc-code-scanner` subagent · **Gate:** yes · **Reviewer:** no
**Scopes:** all, only for brownfield projects (skipped automatically on greenfield)
**Inputs:** `workspace-analysis.md`, the codebase, existing `aidlc-docs/codebase/`
**Outputs (shared, in `aidlc-docs/codebase/`):** `code-scan.md`, `architecture.md`,
`technology-stack.md`, `code-quality.md`; **(in artifact_dir):** `re-summary.md`

## Purpose

Build a trustworthy model of the existing system before changing it. The
result lives in `aidlc-docs/codebase/` so later workflows reuse it instead of
rescanning every time.

## Steps

### 1. Reuse or rescan

If `aidlc-docs/codebase/architecture.md` exists, compare its recorded commit
with `HEAD`:
- Few commits since and none touching the area of this request → propose
  reusing it (optionally a focused scan of the request's area).
- Many changes, or changes in the relevant area → propose a rescan.

Ask with AskUserQuestion: `Reutilizar el análisis existente` / `Escaneo enfocado en <área>` /
`Escaneo completo`. With no prior scan, go straight to a scan — full for
standard/comprehensive depth, focused on the request's area for minimal depth
(bugfix, refactor, poc).

### 2. Scan (subagent)

Dispatch `aidlc-code-scanner` with: repository root, breadth (full or the named
area), depth, output path `aidlc-docs/codebase/code-scan.md`, and the artifact
language. Do not pre-read the source yourself; the scanner does that.

When it returns, check the file exists and has its required sections.

### 3. Synthesize (you, as architect)

Read `code-scan.md` and write, in `aidlc-docs/codebase/`:

- `architecture.md` — first line: `> Escaneado: <ISO date> · commit <short hash>`.
  System context, main components and responsibilities, a Mermaid component
  diagram, main data flows, integration points, architectural style and
  patterns in use.
- `technology-stack.md` — table: area, technology, version, evidence. This is
  the brownfield stack Tech Stack Definition will confirm.
- `code-quality.md` — test coverage situation, lint/format/CI, technical debt
  and risks with severity, and "Zonas frágiles": areas to change with care.

Then, in the artifact dir, `re-summary.md`: what matters for *this* request —
affected components, risks, questions for the person, plus links to the four
shared documents.

### 4. Gate

Emoji: 🔬. Summary: architecture in 2–3 lines, stack, test situation, top
risks for this request. Review: `aidlc-docs/codebase/architecture.md`,
`re-summary.md`. Mention that the analysis is saved for future workflows.
