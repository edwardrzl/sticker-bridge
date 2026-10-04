# Plan de generación de código — U2 — Enlaces y extracción

> Plan que la persona aprueba antes de escribir código. Cambiarlo después de
> aprobado requiere volver a aprobarlo.

## Alcance

- **Unidad:** U2 — Enlaces y extracción
- **Historias / requisitos cubiertos:** US1.3, US2.1, US2.3, US5.1, US6.1 /
  FR1.3, FR1.4, FR2.1–FR2.8, FR6.1–FR6.4
- **Prioridad:** orden por likes (FR2.8) primero, a petición de la persona.
- **Fuera de este plan:** pantallas definitivas, botón "cargar más" en la
  cuadrícula y mensajes al usuario (U5); paquetes (U4).

## Contexto técnico

- **Stack:** el de `project.md`.
- **Postura de pruebas:** mixta — `core` con prueba primero; adaptador y script,
  código y después prueba manual.
- **Comando de pruebas:** `./gradlew :core:test`.
- **Rama:** `feat/u2-extraction` desde `main`.

## Línea base

- `./gradlew test`: 66 pruebas en verde. **Radio de impacto:** medio
  (extractor, script, lector de respuestas).

## Pasos

- [ ] **Paso 1 — Orden por likes (prueba primero)** (FR2.8): `CommentImage.likes`,
  lectura de `digg_count`, `CommentImageOrder`. Casos 4–6. El extractor devuelve las
  imágenes ordenadas.
- [ ] **Paso 2 — Enlaces (prueba primero)** (FR1.3, FR1.4): `PostLinkParser`,
  `PostLink.kind`. Casos 1–3. La pantalla del esqueleto lo usa y rechaza enlaces no
  válidos.
- [ ] **Paso 3 — Política de carga (prueba primero)** (BR-03, BR-04):
  `InitialLoadPolicy`; el extractor la usa. Caso 7.
- [ ] **Paso 4 — Cargar más y fallos** (FR2.4, FR6.1): script con cursor,
  `__stickerBridgeLoadMore`, `fail:`; `loadMore()` en el extractor; botón provisional
  "Cargar más" en el esqueleto; corrección del error tras cerrar.
- [ ] **Paso 5 — Comprobación en el teléfono**: enlace de la persona ordenado por
  likes; cargar más; modo avión; enlace de perfil.

## Riesgos

- Tiempos de "cargar más" desconocidos → se miden en el paso 5.

## Trazabilidad

| Requisito | Pasos | Pruebas |
|---|---|---|
| FR2.8 | 1, 5 | `CommentImageOrderTest`, `CommentResponseParserTest` |
| FR1.3, FR1.4 | 2, 5 | `PostLinkParserTest` |
| FR2.2, BR-03, BR-04 | 3 | `InitialLoadPolicyTest` |
| FR2.4, FR6.1 | 4, 5 | Manual |
