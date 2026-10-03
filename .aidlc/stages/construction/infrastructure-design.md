# Infrastructure Design

**Phase:** Construction (per unit) · **Lead:** operations · **Gate:** yes · **Reviewer:** no
**Scopes:** enterprise, feature — **conditional**
**Inputs:** the unit's `nfr-design.md`, `functional-design.md`, `tech-stack.md`,
`aidlc-docs/memory/project.md` (Deployment)
**Outputs (unit artifact dir):** `infrastructure-design-questions.md` (if gaps), `infrastructure-design.md`

## Purpose

Map the unit onto concrete infrastructure: compute, data stores, networking,
secrets, environments — as code.

## Condition

Run when the unit is deployed or needs infrastructure of its own (a service,
a database, a queue, a bucket, a scheduled job). Skip —
`$AIDLC skip <key> --reason "…"` — for units that run inside another unit's
deployable (a library, a module of a monolith already covered, a UI served by
an existing host), or when the whole project runs only locally and the person
confirmed it in Practices Discovery.

## Steps

### 1. Questions for gaps

Usually settled by project.md (Deployment) and tech-stack.md. Ask only for
what is missing: provider/account, region, environments, IaC tool
(Terraform, CDK, Pulumi, Docker Compose, platform config), sizing, budget.

### 2. Write `infrastructure-design.md`

```markdown
# Diseño de infraestructura — <unidad>

## Recursos
| Recurso | Servicio / tecnología | Configuración | Entorno(s) | Costo mensual estimado |

## Diagrama
<Mermaid flowchart: clients, entry point, compute, data, external services.>

## Redes y acceso
<Public/private exposure, ports, CORS, IAM/roles and least privilege.>

## Secretos y configuración
<Where secrets live (never in code), how they reach the app.>

## Entornos
| Aspecto | dev | staging | prod |

## Infraestructura como código
<Tool, folder where the IaC will live, modules/stacks to create.>

## Respaldo y recuperación
```

Costs are estimates; say so. Never create real cloud resources in this stage.

### 3. Gate

Emoji: ☁️. Summary: resources, environments, estimated monthly cost, IaC
approach. Review: `infrastructure-design.md`. Options: Aprobar / Solicitar cambios.
