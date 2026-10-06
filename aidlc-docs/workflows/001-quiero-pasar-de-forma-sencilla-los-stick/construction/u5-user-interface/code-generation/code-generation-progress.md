# Avance del plan — U5 — Interfaz completa

Rama: `feat/u5-ui`. Los pasos 6 a 8 se añadieron al plan el 2026-10-05, durante la
prueba de la persona en su teléfono; cada uno quedó registrado como decisión.

- [x] **Paso 1 — `SearchViewModel` y su prueba.** Búsqueda, selección, "cargar más",
  guardado e importación por el mismo caso de uso.
- [x] **Paso 2 — `PacksViewModel` y su prueba.** Lista con estado de WhatsApp y quitar
  stickers.
- [x] **Paso 3 — Pantallas Compose.** Entrada, "Stickers del video" y "Mis paquetes";
  cargador de miniaturas; textos en `strings.xml`; navegación en `MainActivity`.
- [x] **Paso 4 — Compartir e importar.** `ACTION_SEND` en el manifiesto y `onNewIntent`;
  selector de fotos del sistema.
- [x] **Paso 5 — Pantalla de prueba retirada** y comprobación en el teléfono (abajo).
- **Paso 6 — Sesión opcional de TikTok: descartado.** Se construyó, se probó con una
  sesión real y no cambiaba lo que TikTok entrega; el código se retiró (ADR-007).
- [x] **Paso 7 — Pedir los comentarios como la app de TikTok.** Un cambio en
  `comment-capture.js`; enmienda en ADR-002.
- [x] **Paso 8 — Paquetes desde un solo sticker (FR5.12).** `PackPadding` en `core`;
  desaparecen "faltan N" y la acción `Waiting`.

- [x] **Paso 9 — Stickers de las respuestas (FR2.10)**, en la rama `feat/comment-replies`.
  El lector devuelve los comentarios con respuestas; `ReplyQueue` elige los 3 más votados
  por toque; el script pide `/api/comment/list/reply/`; botón "Buscar en las respuestas".
  **Sin comprobar en el teléfono** (estaba desconectado): se validó ejecutando el script
  real en Node contra TikTok, donde un toque trajo 42 imágenes nuevas de 3 comentarios.

- [x] **Paso 10 — Diseño visual**, en la rama `feat/visual-design`. Tema propio claro y
  oscuro, icono de la app, barra superior con flecha, likes abreviados (`LikeCount`),
  marca de selección, estado del paquete como etiqueta, botón para pegar el enlace.
  Verificado con 9 capturas generadas en la computadora; **sin ver en el teléfono**.

## Resultados verificados

- `./gradlew test`: 122 pruebas (101 `core`, 21 `app`), 0 fallos; ktlint, detekt y Android
  Lint sin incidencias. Con el paso 9: 133 pruebas (109 `core`, 24 `app`), 0 fallos.

## Comprobación en el teléfono (OPPO A78, Android 15)

| Qué | Resultado |
|---|---|
| Buscar un video por enlace y ver la cuadrícula ordenada por likes | Hecho por la persona |
| Stickers que TikTok ocultaba a la web (paso 7) | 15 imágenes donde llegaban 4; "ya salen todos" |
| Guardar un solo sticker animado y agregarlo a WhatsApp (paso 8) | WhatsApp lo muestra una sola vez |
| Datos de prueba antiguos | Borrados reinstalando la app |

## Pendiente de comprobar por la persona

La persona delegó las aprobaciones y dio la unidad por buena, pero estos puntos de
`docs/manual-checklist.md` no tienen todavía un resultado anotado:

- **Respuestas (paso 9):** "Buscar en las respuestas" añade stickers nuevos y conserva los
  elegidos; nunca se ejecutó en el teléfono.
- Compartir desde TikTok (se probó pegando el enlace).
- Importar de la galería y quitar un sticker desde "Mis paquetes".
- Criterio de salida del alcance: 5 videos, menos de 1 minuto cada uno.
- Quitar en WhatsApp los paquetes de relleno de las pruebas antiguas, si siguen ahí.
