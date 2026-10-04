# Avance del plan — U4 — Paquetes y WhatsApp

Rama: `feat/u4-packs`. Todos los pasos en el commit `9f35032`.

- [x] **Paso 1 — Puerto del repositorio.** `FileSource`, `PackFile`, commit en tres fases.
- [x] **Paso 2 — Reparto al llenarse.** El paquete siguiente nace con los 2 últimos del lleno
  más el nuevo; el lleno queda con 28 y sube de versión.
- [x] **Paso 3 — Quitar stickers.** Versión +1, ícono regenerado si cambia el primero,
  paquete eliminado al quitar el último.
- [x] **Paso 4 — Caso de uso de guardado.** `SaveStickersUseCase`, `PackAction`
  (`Waiting`, `OpenWhatsApp`, `Invalid`), `SaveResult`.
- [x] **Paso 5 — Pantalla de prueba con el caso de uso.** Sin copias de relleno; una
  confirmación de WhatsApp por paquete. Instalado en el teléfono; la comprobación en el
  teléfono se hace junto con U5 (la cuadrícula de selección).

## Resultados verificados

- `./gradlew test`: 101 pruebas (95 `core`, 6 `app`), 0 fallos; ktlint, detekt y Android
  Lint sin incidencias.
