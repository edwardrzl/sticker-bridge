# Scope Definition

**Phase:** Ideation · **Lead:** product · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature, mvp
**Inputs:** `intent-statement.md`, `feasibility-assessment.md` (if produced)
**Outputs:** `scope-definition-questions.md`, `scope-document.md`, `backlog.md`

## Purpose

Draw the boundary of this workflow: what is in, what is out, and a
prioritized backlog of capabilities. For an MVP, this is where "minimum"
is decided.

## Steps

### 1. Draft the capability list

From the intent, list candidate capabilities (coarse features, not stories).
For each, a one-line value statement and a rough size (S/M/L).

### 2. Interview

Create `scope-definition-questions.md`. Topic areas:
- Which capabilities are in this release (multi-select), which are later.
- Prioritization approach: MoSCoW by default; offer RICE/value-vs-effort when
  there are many candidates (`.aidlc/knowledge/product/prioritization-frameworks.md`).
- Hard boundaries: platforms (web/mobile/API), languages/locales, user roles,
  integrations included vs mocked.
- Release criteria: what must be true to call this done.

For mvp scope, push for the smallest set that proves the intent's success
metric; say which capabilities you would cut and why.

### 3. Write the documents

`scope-document.md`:
```markdown
# Documento de alcance
## Resumen
## Dentro del alcance
| ID | Capacidad | Valor | Tamaño | Prioridad (MoSCoW) |
| CAP-01 | | | | Must |
## Fuera del alcance (y por qué)
## Límites (plataformas, roles, integraciones)
## Criterios de salida (release)
## Supuestos y preguntas abiertas
```

`backlog.md`: capabilities ordered by priority, with later-release items
at the bottom under "Futuro".

### 4. Gate

Emoji: 📐. Summary: count of in/out capabilities, the Must list, release
criteria. Review: `scope-document.md`, `backlog.md`.
