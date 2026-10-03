# AI-DLC Principles

AI-DLC (AI-Driven Development Life Cycle) structures AI-assisted development
into traceable phases while keeping the person in control of every decision.

1. **The person decides, the AI executes.** Every material decision passes an
   approval gate. The AI proposes, explains trade-offs and waits.
2. **Questions before assumptions.** When in doubt, ask. An unconfirmed
   inference is written as an assumption, never as a decision.
3. **Specs before code.** Intent → requirements → stack → design → units →
   plan → code. Code is written only after its plan is approved.
4. **Adaptive depth.** Scopes decide which stages run; depth decides how much
   detail each stage writes. A bug fix does not get an enterprise ceremony.
5. **Traceable artifacts.** Every stage writes versioned markdown in
   `aidlc-docs/`, with stable IDs (FR, NFR, US, U, ADR) carried downstream.
6. **Expert personas.** Each stage is led by a domain expert voice (product,
   architect, developer, quality, security, operations, delivery).
7. **Contradictions are resolved, not carried.** Answers are cross-checked;
   conflicts are surfaced and settled inside the stage.
8. **Memory that sticks.** Affirmed decisions (stack, practices, rules) are
   promoted to `aidlc-docs/memory/project.md`, which every later session reads.

## Phases

| Phase | Purpose | Outcome |
|---|---|---|
| Initialization | Detect the workspace, create the workflow | Workflow ready |
| Ideation | Validate the idea: intent, feasibility, scope | Approved scope and backlog |
| Inception | Elaborate: codebase, practices, requirements, stack, stories, design, units, plan | Buildable specification |
| Construction | Build per unit: design, NFRs, code, tests, CI | Working, tested code |
| Operation | Pipelines, observability, deployment | Running system |

## Bolts and units

A **unit of work** is an independently buildable, testable piece (a service,
module, UI surface, library). A **bolt** is one short build cycle over a unit:
design → plan → code → tests, closed by a human gate. Bolts are measured in
hours or days, not weeks.
