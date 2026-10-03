# Observability Setup

**Phase:** Operation · **Lead:** operations · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise
**Inputs:** NFR requirements and designs (observability sections), `infrastructure-design.md`,
`deployment-pipeline.md`, `tech-stack.md`
**Outputs:** artifact dir: `observability.md`, `runbook.md`; logging/metrics/alert config in the project tree

## Purpose

Make it possible to answer "is it working for users right now, and if not,
why?" — with SLOs, useful signals, actionable alerts and a runbook.

## Steps

### 1. Define SLOs

From the NFRs: SLIs (availability, latency, error rate, freshness) and SLO
targets per user-facing capability, with error budgets
(`.aidlc/knowledge/operations/slo-sli-patterns.md`). Ask the person to confirm
targets that were never stated as numbers.

### 2. Implement

- Structured logging with correlation/request IDs (verify the code already
  does what NFR Design specified; add what is missing).
- Metrics for the golden signals (latency, traffic, errors, saturation).
- Tracing where there are cross-service calls.
- Dashboards and alert rules as code for the chosen platform, alerting only
  on symptoms users feel, each alert linked to a runbook section.
- Health/readiness endpoints if missing.

### 3. Write the documents

`observability.md`: SLO table, signals and where they are emitted, dashboards,
alerts (condition, severity, who is notified).
`runbook.md`: per alert — what it means, first checks, mitigation, escalation;
plus how to roll back (link to deployment-pipeline.md).

### 4. Gate

Emoji: 📈. Summary: SLOs, alerts, dashboards, runbook entries. Review:
`observability.md`, `runbook.md`. Options: Aprobar / Solicitar cambios.
