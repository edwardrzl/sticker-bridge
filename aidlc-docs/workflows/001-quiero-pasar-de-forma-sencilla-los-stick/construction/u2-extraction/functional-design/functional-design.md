# Diseño funcional — U2 — Enlaces y extracción

Qué hace la extracción completa: reconocer enlaces de TikTok, cargar los
comentarios por tandas, ordenar sus imágenes por likes, cargar más bajo demanda y
distinguir los fallos. Parte de lo construido y verificado en U1 y U3 (petición
de comentarios desde la página, tandas iniciales).

## Alcance de la unidad

Historias: US1.3, US2.1, US2.3, US5.1, US6.1 · Requisitos: FR1.3, FR1.4,
FR2.1–FR2.8, FR6.1–FR6.4 · Reglas: BR-01 a BR-05 · NFR4, NFR6, NFR11, NFR12.

**Prioridad acordada con la persona (2026-10-04): el orden por likes (FR2.8)
primero.**

## Hechos verificados en el teléfono

- La respuesta real trae `digg_count` (likes) en cada comentario y `total` (número
  de comentarios); TikTok web los entrega sin ordenar por likes (19, 3, 53, 7…).
- La petición `/api/comment/list/?aid=1988&aweme_id=…&count=20&cursor=…` funciona
  desde la página para videos y publicaciones de fotos.
- Los enlaces cortos `vt.tiktok.com/…` redirigen a `www.tiktok.com/@…/photo/…` o
  `/video/…`.

## Modelo de datos

| Tipo | Cambio |
|---|---|
| `CommentImage` | Nuevo campo `likes: Long` (el `digg_count` de su comentario; 0 si falta) |
| `PostLink` | `url` normalizada y `kind`: `VIDEO`, `PHOTO` o `SHORT` (redirige) |
| `ExtractionPage` | Sin cambios: `images` (ya ordenadas) y `hasMore` |

## Lógica de negocio

### OP-1 — Reconocer el enlace (`PostLinkParser`, BR-01)

Busca en el texto la primera dirección que encaje con alguna de estas formas y la
normaliza a `https://`:

| Forma | Tipo |
|---|---|
| `tiktok.com/@usuario/video/<id>` (con o sin `www.`, `m.`) | VIDEO |
| `tiktok.com/@usuario/photo/<id>` | PHOTO |
| `m.tiktok.com/v/<id>` | VIDEO |
| `vm.tiktok.com/<código>`, `vt.tiktok.com/<código>`, `tiktok.com/t/<código>` | SHORT |

Cualquier otra cosa (otro sitio, perfil de TikTok, texto sin enlace) → sin enlace.
Se ignoran los parámetros (`?is_from_webapp=…`).

### OP-2 — Orden por likes (`CommentImageOrder`, FR2.8)

Orden estable descendente por `likes` sobre la lista completa acumulada. Duplicados
(misma dirección, BR-02): se conserva la primera aparición con el mayor número de
likes.

### OP-3 — Carga inicial (`InitialLoadPolicy`, BR-03, BR-04)

Regla pura, sin reloj propio:
- falla con `Timeout` si no llega la primera tanda en 30 s;
- termina al tener 3 tandas, o cuando TikTok indica que no hay más, o a los 10 s
  desde la primera tanda.

Hoy esta lógica vive en el adaptador (adelantada en U3); pasa a `core` con pruebas.

### OP-4 — Cargar más (FR2.4)

`session.loadMore()` pide al script la siguiente página con el último `cursor`
recibido y espera una tanda (15 s). Devuelve la lista completa, reordenada, y
`hasMore`. Si TikTok ya dijo que no hay más, devuelve lo mismo sin pedir nada.

### OP-5 — Clasificación de fallos (FR6.1)

| Situación | Cómo se detecta | Error |
|---|---|---|
| Sin conexión | Error de red de la página principal (`ERROR_HOST_LOOKUP`, `ERROR_CONNECT`, `ERROR_TIMEOUT`) | `NoConnection` |
| Publicación inexistente | HTTP de error en la página principal, o sin identificador de publicación en la dirección final | `PostUnavailable` |
| Pide sesión o verificación | Redirección a `/login`, `/signup` o a una página de verificación | `CommentsBlocked` |
| Sin respuesta | Ninguna tanda en 30 s | `Timeout` |
| Formato desconocido | Respuesta sin `comments`, o la petición de comentarios falla 3 veces con estado distinto de 0 | `UnexpectedFormat` |

El script informa sus fallos con un mensaje `fail:` además de `diag:`.

### OP-6 — Corrección

El script ya no lanza "Cannot read properties of undefined (reading 'json')"
cuando la sesión se cierra a mitad de una petición.

## Interfaces

```kotlin
object PostLinkParser { fun parse(text: String): PostLink? }
object CommentImageOrder { fun byLikes(images: List<CommentImage>): List<CommentImage> }
class InitialLoadPolicy { fun decide(batches: Int, hasMore: Boolean, msSinceStart: Long, msSinceFirstBatch: Long?): LoadDecision }
// ExtractionSession sin cambios de firma; loadMore() pasa a funcionar.
```

Mensajes script → app: `body:<json>`, `diag:<nota>`, `fail:<motivo>`. App → script:
`window.__stickerBridgeLoadMore()` mediante `evaluateJavascript`.

## Validaciones y errores

Ver OP-5. Ningún fallo cierra la app ni toca los paquetes (FR6.4).

## Casos de prueba derivados

Automáticos (`core`):

1. Enlaces válidos: las seis formas, solas y dentro de un texto con más palabras.
2. Enlaces inválidos: otro sitio, perfil de TikTok, texto sin enlace.
3. Normalización: se quitan parámetros y se fuerza `https`.
4. El lector toma `digg_count` como likes; 0 si falta.
5. Orden: 53, 19, 3 likes → en ese orden; empate → orden original.
6. Duplicado con distintos likes → una sola entrada con el mayor.
7. Política: sin tandas a los 30 s → `Timeout`; 3 tandas → terminar; `hasMore`
   falso → terminar; 10 s tras la primera → terminar; si no, esperar.

Manuales (teléfono): el enlace de la persona muestra primero la imagen del
comentario con más likes; "cargar más" añade imágenes; modo avión → "Sin
conexión"; enlace de perfil → rechazado.

## Trazabilidad

| Historia / FR | Sección |
|---|---|
| US1.3 · FR1.3, FR1.4 | OP-1, casos 1–3 |
| US2.1 · FR2.1, FR2.2, FR2.5, FR2.8 | OP-2, OP-3, casos 4–7 |
| US2.3 · FR2.4 | OP-4 |
| US5.1 · FR6.1–FR6.4 | OP-5 |
| US6.1 · NFR12 | Mensajes `diag:`/`fail:` |

## Supuestos

- [assumption] TikTok no expone un orden por likes en la API web; ordenar en el
  teléfono solo cubre los comentarios cargados (hasta 60 al inicio, más con
  "cargar más").
