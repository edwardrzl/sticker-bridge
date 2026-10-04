# Avance del plan — U2 — Enlaces y extracción

Copia de los pasos del plan aprobado con su estado real. Rama: `feat/u2-extraction`.

- [x] **Paso 1 — Orden por likes.** `CommentImage.likes` (`digg_count`), `CommentImageOrder`;
  el extractor devuelve todo lo cargado ordenado. Commit `90d529e`.
- [x] **Paso 2 — Enlaces.** `PostLinkParser` y `PostKind`; la pantalla de prueba rechaza
  enlaces no válidos. Commit `f4c68ce`.
- [x] **Paso 3 — Política de carga.** `InitialLoadPolicy` en `core`; el extractor la usa.
  Commit `ce736a7`.
- [x] **Paso 4 — Cargar más y fallos.** Cursor y `__stickerBridgeLoadMore` en el script,
  `loadMore()`, mensajes `fail:`; botón provisional "Cargar más". Commit `ce736a7`.
- [ ] **Paso 5 — Comprobación en el teléfono.** Instalado; pendiente de que la persona
  confirme el orden por likes, "cargar más", el rechazo de un perfil y el modo avión.

## Resultados verificados

- `./gradlew test`: 85 pruebas (80 `core`, 5 `app`), 0 fallos; ktlint, detekt y Android Lint
  sin incidencias.
- Respuesta real del enlace de la persona: `digg_count` presente; orden de TikTok
  19, 3, 53, 7… (no por likes), `total` 71 comentarios.

## Decisiones de la persona durante la unidad (2026-10-04)

- FR2.8 (orden por likes) añadido y priorizado.
- Organización en WhatsApp: colección que crece, eligiendo cada sticker y pudiendo
  quitarlos. "Quitar stickers" pasa de Futuro a esta versión (U4/U5).
- Pruebas en su teléfono, sin emulador.
