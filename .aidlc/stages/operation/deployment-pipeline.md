# Deployment Pipeline

**Phase:** Operation · **Lead:** operations · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature — **conditional**
**Inputs:** `infrastructure-design.md` (per unit), `ci-pipeline.md`, `tech-stack.md`,
`aidlc-docs/memory/project.md` (Deployment)
**Outputs:** artifact dir: `deployment-pipeline.md`; pipeline and IaC files in the project tree

## Purpose

Automate getting a verified build into each environment, with a rollback
path, so deploying is boring and repeatable.

## Condition

Skip — `$AIDLC skip deployment-pipeline --reason "…"` — when the project is not
deployed anywhere (library published manually, local-only tool) and the
person confirms it.

## Steps

### 1. Confirm the approach

From project.md (Deployment) and the infrastructure designs: target platform,
environments, promotion flow (dev → staging → prod), approval before prod,
strategy (rolling, blue/green, canary — `.aidlc/knowledge/operations/deployment-strategies.md`).
Ask only what is not settled.

### 2. Write the pipeline and IaC

- Deployment workflow that consumes the CI build artifact, deploys to each
  environment, runs smoke tests after deploy, and requires a manual approval
  before production.
- Infrastructure as code for the resources in the infrastructure designs, in
  the folder they named.
- Database migrations step with a documented rollback.
- Secrets referenced by name only.

Validate locally what can be validated (`terraform validate`/`plan` without
applying, `cdk synth`, `docker compose config`, workflow linters). **Never
apply infrastructure or deploy in this stage.**

### 3. Write `deployment-pipeline.md`

Flow diagram, environments, what triggers each deploy, approvals, smoke
tests, rollback procedure step by step, required secrets and permissions,
estimated cost.

### 4. Gate

Emoji: 🚀. Summary: flow, environments, rollback, what the person must
configure. Review: `deployment-pipeline.md` and file paths. Options: Aprobar /
Solicitar cambios.
