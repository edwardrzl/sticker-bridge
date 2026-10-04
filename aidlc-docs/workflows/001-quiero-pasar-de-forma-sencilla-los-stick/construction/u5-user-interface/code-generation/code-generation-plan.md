# Plan de generación de código — U5 — Interfaz completa

> Plan que la persona aprueba antes de escribir código. Cambiarlo después de
> aprobado requiere volver a aprobarlo.

## Alcance

- **Unidad:** U5 — Interfaz completa
- **Requisitos:** FR1.1, FR1.2, FR2.3, FR2.4, FR3.1–FR3.4, FR5.8, FR5.10, FR5.11,
  FR6.1–FR6.3, FR7.1–FR7.3
- **Prioridad de la persona:** elegir cada sticker antes de mandarlo a WhatsApp, y
  poder quitar stickers.

## Contexto técnico

- **Postura de pruebas:** ViewModels con puertos falsos en la JVM (prueba después);
  apariencia con la lista manual.
- **Comando:** `./gradlew test`.
- **Rama:** `feat/u5-ui` desde `main`.

## Pasos

- [ ] **Paso 1 — `SearchViewModel` y su prueba** (casos 1–6).
- [ ] **Paso 2 — `PacksViewModel` y su prueba** (caso 7).
- [ ] **Paso 3 — Pantallas Compose** (P1, P2, P3), cargador de miniaturas, textos en
  `strings.xml`, `MainViewModel` y navegación.
- [ ] **Paso 4 — Compartir e importar**: `ACTION_SEND` en el manifiesto y
  `onNewIntent`; selector de fotos.
- [ ] **Paso 5 — Retirar la pantalla de prueba** y comprobación en el teléfono
  (criterio de salida).

## Trazabilidad

| Requisito | Pasos | Pruebas |
|---|---|---|
| FR3.1–FR3.4, FR2.4, FR6.1 | 1, 3 | `SearchViewModelTest` |
| FR5.8, FR5.11 | 2, 3 | `PacksViewModelTest` |
| FR1.1, FR7 | 4 | Manual |
