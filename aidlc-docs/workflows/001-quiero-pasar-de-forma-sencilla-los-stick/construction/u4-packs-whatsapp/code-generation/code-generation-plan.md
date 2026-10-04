# Plan de generación de código — U4 — Paquetes y WhatsApp

> Plan que la persona aprueba antes de escribir código. Cambiarlo después de
> aprobado requiere volver a aprobarlo.

## Alcance

- **Unidad:** U4 — Paquetes y WhatsApp
- **Requisitos:** FR4.7, FR4.8, FR5.1–FR5.11, FR7.2 · CAP-11 (quitar stickers)
- **Fuera:** pantallas definitivas, cuadrícula de selección y lista de paquetes (U5).

## Contexto técnico

- **Postura de pruebas:** mixta; reglas y caso de uso en `core` con prueba primero.
- **Comando:** `./gradlew test`.
- **Rama:** `feat/u4-packs` desde `main`.

## Línea base

- `./gradlew test`: 85 pruebas en verde. **Radio de impacto:** medio (puerto del
  repositorio, `PackService`, repositorio de archivos, pantalla de prueba).

## Pasos

- [ ] **Paso 1 — Puerto del repositorio** (NFR9): `FileSource`, `PackFile`,
  `commit(packs, staged, obsolete)`; adaptar `PackService`, el repositorio de
  archivos y sus pruebas (caso 9).
- [ ] **Paso 2 — Reparto al llenarse (prueba primero)** (FR5.2): casos 1–2.
- [ ] **Paso 3 — Quitar stickers (prueba primero)** (FR5.11): casos 3–5.
- [ ] **Paso 4 — Caso de uso de guardado (prueba primero)** (FR4.8, FR5.5, FR5.9):
  casos 6–8.
- [ ] **Paso 5 — Pantalla de prueba con el caso de uso** y comprobación en el
  teléfono (un solo UPDATE por paquete afectado).

## Trazabilidad

| Requisito | Pasos | Pruebas |
|---|---|---|
| FR5.2 | 2 | `PackServiceTest` |
| FR5.11 | 3 | `PackServiceTest` |
| FR4.8, FR5.5, FR5.9 | 4 | `SaveStickersUseCaseTest` |
| NFR9 | 1 | `FilePackRepositoryTest` |
