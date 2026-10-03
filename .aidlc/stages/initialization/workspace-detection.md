# Workspace Detection

**Phase:** Initialization · **Lead:** developer · **Gate:** no (auto-proceeds) · **Reviewer:** no
**Scopes:** all
**Inputs:** the workflow's `request.md`, `$AIDLC detect` output, the project tree
**Outputs:** `workspace-analysis.md`

## Purpose

Record what already exists in the project so every later stage starts from
facts: greenfield or brownfield, languages, frameworks, build and test
tooling, and whether a previous reverse-engineering scan can be reused.

## Steps

### 1. Detect

Run `$AIDLC detect` (it is fast and read-only). Then look briefly at the
project root yourself: README, manifest files, top-level directories, CI
config, `.env.example`, Dockerfiles. Do not read source files in depth here —
that is Reverse Engineering's job.

### 2. Check prior knowledge

- `aidlc-docs/codebase/` — does a previous code scan exist? Note its date and
  the git commit it was made at (first lines of `codebase/architecture.md`),
  and how many commits have happened since (`git rev-list --count <commit>..HEAD`
  when git is available).
- `aidlc-docs/memory/project.md` — which sections are already defined.
- Other workflows in `aidlc-docs/workflows/` — list them with status.

### 3. Write `workspace-analysis.md`

In the artifact language:

```markdown
# Análisis del workspace

- **Tipo de proyecto:** greenfield | brownfield (evidencia: …)
- **Lenguajes:** …
- **Frameworks y librerías principales:** … (evidencia: archivos)
- **Build / gestor de paquetes:** …
- **Pruebas:** framework, ubicación, comando si es evidente
- **CI/CD:** archivos encontrados o "ninguno"
- **Control de versiones:** git sí/no, rama actual, último commit
- **Conocimiento previo:** escaneo en aidlc-docs/codebase (fecha, commit, N commits desde entonces) | ninguno
- **Memoria del proyecto:** secciones definidas en project.md
- **Otros workflows:** …

## Observaciones
<Anything surprising: monorepo, several apps, generated code, no tests, secrets committed…>
```

### 4. Close

If detection contradicts the workflow's project type (e.g. the workflow says
greenfield but real source code exists), say so to the person in one line
and ask whether to continue as is — the type decides whether Reverse
Engineering runs. Otherwise, `$AIDLC complete workspace-detection` and continue
without a gate. Tell the person in one or two lines what you found.
