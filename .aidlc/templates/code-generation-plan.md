# Plan de generación de código — <unidad o cambio>

> Plan que la persona aprueba antes de escribir código. Cambiarlo después de
> aprobado requiere volver a aprobarlo.

## Alcance
- **Unidad:** <U1 — nombre> (o "cambio completo" si no hay unidades)
- **Historias / requisitos cubiertos:** <US1.1, US1.2 / FR1, FR3>
- **Fuera de este plan:** <lo que explícitamente no se toca>

## Contexto técnico
- **Stack:** <del project.md: lenguaje, framework, versiones>
- **Postura de pruebas:** <TDD | test-after | BDD | …> — <orden en una frase>
- **Estrategia de pruebas:** <minimal | standard | comprehensive> + piso del scope
- **Comando de pruebas de esta unidad:** `<comando exacto, acotado a la unidad>`

## Línea base (solo proyectos existentes)
- Suite actual: <N> pruebas, <p> pasan, <f> fallan (preexistentes), <s> omitidas, cobertura <c>%
- **Radio de impacto:** <bajo | medio | alto>

| Archivo | Cambio | Consumidores / pruebas afectadas |
|---|---|---|
| src/… | modificar | … |

## Pasos

- [ ] **Paso 1 — <preparación del runner de pruebas / scaffolding>** (<US/FR>)
  - Archivos: `…`
  - Hecho cuando: <criterio verificable>
- [ ] **Paso 2 — <capa / comportamiento>** (<US/FR>)
  - Pruebas: `…` (<qué casos>)
  - Archivos: `…`
  - Hecho cuando: <las pruebas X pasan>
- [ ] **Paso N — Documentación y configuración** 
  - `README`, `.env.example`, …

## Riesgos y decisiones abiertas
- <riesgo> → <mitigación>

## Trazabilidad

| Historia / requisito | Pasos | Pruebas |
|---|---|---|
| US1.1 | 2, 3 | tests/… |
