# Deployment Execution

**Phase:** Operation · **Lead:** operations · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise — **conditional**
**Inputs:** `deployment-pipeline.md`, `runbook.md`, `build-and-test-summary.md`
**Outputs:** artifact dir: `deployment-checklist.md`, `deployment-record.md`

## Purpose

Take the release to production deliberately: a checklist, an explicit go
from the person, verification after deploy, and a record of what happened.

## Condition

Skip when the person will deploy later or outside this workflow (say so and
record the reason). This stage never deploys without an explicit "go" typed
by the person in this conversation.

## Steps

### 1. Pre-deployment checklist — `deployment-checklist.md`

- Build and tests green on the commit to deploy (link the CI run).
- Migrations reviewed; backup taken or confirmed.
- Secrets and configuration present in the target environment.
- Rollback procedure rehearsed or at least reviewed.
- Stakeholders informed; deployment window.

Walk the person through it; they confirm each item.

### 2. Go / no-go

Ask with AskUserQuestion: `Desplegar ahora` / `No desplegar todavía`. **End the
turn.** Only on "Desplegar ahora" trigger the deployment through the pipeline
(prefer the pipeline over manual commands). Anything irreversible outside the
pipeline needs its own explicit confirmation.

### 3. Verify

Smoke tests, health endpoints, dashboards and error rates for a short watch
window. If something is wrong, follow the rollback procedure and tell the
person immediately.

### 4. Record — `deployment-record.md`

Version/commit deployed, environment, time, who approved, checks run and
results, incidents, rollback (if any), follow-ups.

### 5. Gate

Emoji: 🏁. Summary: what was deployed, verification results, follow-ups.
Review: `deployment-record.md`. Options: Aprobar / Solicitar cambios.
