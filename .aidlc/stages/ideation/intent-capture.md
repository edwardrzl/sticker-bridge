# Intent Capture

**Phase:** Ideation · **Lead:** product · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature, mvp
**Inputs:** `request.md`, `workspace-analysis.md`
**Outputs:** `intent-capture-questions.md`, `intent-statement.md`

## Purpose

Turn the person's request into a clear intent: the problem, for whom, why
now, what success looks like. Everything downstream is checked against this
document.

## Steps

### 1. Analyze the request

Read `request.md`. Identify what is already clear and what is missing among:
problem, target users, desired outcome, success metrics, constraints (time,
budget, team, technology already decided), and non-goals.

### 2. Interview

Create `intent-capture-questions.md` (protocol §3). Topic areas — ask only
what the request does not already answer:

- **Problem** — what hurts today, for whom, how they cope now.
- **Users** — primary and secondary users; who pays, who uses, who administers.
- **Outcome** — what is different for users when this works.
- **Success** — how we will measure it (a number or an observable fact).
- **Why now** — trigger, deadline, opportunity.
- **Constraints** — fixed deadline, budget, team size/skills, mandated
  technology or platform, regulatory context.
- **Non-goals** — what this explicitly is not.

Follow the answering modes, answer analysis and summary confirmation of the
protocol.

### 3. Write `intent-statement.md`

```markdown
# Declaración de intención

## Resumen
<2–3 sentences anyone can understand.>

## Problema
## Usuarios
| Tipo | Quién | Necesidad principal |
## Resultado esperado
## Métricas de éxito
| Métrica | Hoy | Objetivo | Cómo se mide |
## Restricciones
## No-objetivos
## Supuestos y preguntas abiertas
```

### 4. Gate

Completion emoji: 🎯. Summary: the one-sentence intent, users, top success
metric, key constraints. Review: `intent-statement.md`.
