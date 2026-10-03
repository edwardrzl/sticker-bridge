# CI Pipeline

**Phase:** Construction · **Lead:** operations · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature
**Inputs:** `test-instructions.md`, `build-and-test-summary.md`, `tech-stack.md`,
`aidlc-docs/memory/project.md` (Way of Working, Code Style), existing CI config (brownfield)
**Outputs:** artifact dir: `ci-pipeline.md`; CI configuration files in the project tree

## Purpose

Make every push prove what Build and Test proved by hand: build, lint, tests,
coverage and basic security checks, automatically.

## Steps

### 1. Decide the platform

Use what the project already has (brownfield) or what project.md says. If
nothing is decided, ask one question: GitHub Actions / GitLab CI / Azure
Pipelines / other. Also confirm the trigger policy (push to main, PRs,
branches) if the practices do not state it.

### 2. Write the pipeline

Stages in the CI config:
1. Checkout and dependency install with caching.
2. Lint / format check / type check.
3. Unit tests, then integration tests (with service containers such as a
   database when needed).
4. Coverage report with the scope's floor enforced (80% on new code for
   mvp/feature/enterprise) — never below the target the person set.
5. Security: dependency audit and secret scanning (built-in tools of the
   platform where possible).
6. Build artifact (package, container image) — no deploy here.

Pin action/image versions. No secrets in the file; reference platform
secrets by name and list them.

### 3. Validate locally

Validate syntax where a local tool exists (e.g. `actionlint`), and run the
same commands the pipeline runs to make sure they pass. You cannot run the
hosted pipeline; say so and tell the person how to see the first run.

### 4. Write `ci-pipeline.md`

Platform, file path(s), jobs and what each checks, required secrets and
where to configure them, how to read failures, branch protection
recommendation.

### 5. Gate

Emoji: 🔁. Summary: jobs, checks enforced, secrets the person must add.
Review: `ci-pipeline.md` and the config file path. Options: Aprobar / Solicitar cambios.
