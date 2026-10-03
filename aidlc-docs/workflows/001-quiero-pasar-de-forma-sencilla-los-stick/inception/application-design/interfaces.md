# Interfaces

Las operaciones en cada frontera entre componentes: entradas, salidas,
errores y qué historias las usan. Son llamadas dentro del mismo proceso; no
hay API de red propia. Las firmas son orientativas (Kotlin); el diseño
funcional de cada unidad las detalla.

## Convenciones

- Las operaciones lentas son funciones `suspend` y respetan la cancelación.
- Los errores esperados se devuelven como resultado con tipo, no como
  excepciones; las excepciones quedan para fallos de programación.
- Los puertos se declaran en `core`; los adaptadores viven en `app`.

## CMP-01 — Lector de enlaces

| Operación | Entrada | Salida | Errores | Historias |
|---|---|---|---|---|
| `PostLinkParser.parse(text)` | Texto compartido o pegado | `PostLink` o nada | Ninguno: sin enlace válido devuelve nada | US1.1, US1.2, US1.3 |

## CMP-02 / CMP-03 — Extracción (puerto `CommentImageExtractor`)

| Operación | Entrada | Salida | Errores | Historias |
|---|---|---|---|---|
| `open(link)` | `PostLink` | Una sesión de extracción (`ExtractionSession`) | — | US2.1 |
| `session.loadInitial()` | — | `ExtractionPage` con lo encontrado al cumplirse BR-03 | `ExtractionError` (BR-04 y FR6.1) | US2.1, US2.4, US5.1 |
| `session.loadMore()` | — | `ExtractionPage` con una tanda más | `ExtractionError` | US2.3 |
| `session.close()` | — | Libera el navegador interno y borra cookies y datos | — | US2.1 |

Piezas de `core` que el adaptador usa:

| Operación | Entrada | Salida | Errores |
|---|---|---|---|
| `CommentResponseParser.parse(json)` | Cuerpo de una respuesta de lista de comentarios | Imágenes y si hay más | `UnexpectedFormat` |
| `InitialLoadPolicy` | Tandas recibidas y tiempos | Seguir o terminar (BR-03, BR-04) | — |
| `HostAllowlist.isAllowed(host)` | Nombre de dominio | Sí o no (BR-05) | — |

La sesión elimina duplicados entre tandas (BR-02). Cancelar la corrutina que
llama equivale a `close()`.

## CMP-04 / CMP-05 — Conversión

| Operación | Entrada | Salida | Errores | Historias |
|---|---|---|---|---|
| `StickerConverter.convert(source)` | Referencia a la imagen: dirección remota o imagen de la galería | `ConvertedSticker` | `ConversionError`: `DownloadFailed`, `UnsupportedImage`, `TooLarge` | US3.1, US3.2, US3.3, US5.2 |
| Puerto `ImageSource.open(ref)` | Referencia | Imagen decodificable con su `SourceImageInfo` y acceso a cuadros | `DownloadFailed`, `UnsupportedImage` | — |
| Puerto `StickerEncoder.encodeStatic(frame, plan, quality)` | Primer cuadro, plan, calidad | Archivo temporal y su tamaño | `UnsupportedImage` | — |
| Puerto `StickerEncoder.encodeAnimated(frames, plan, quality, fps)` | Cuadros, plan, calidad, cuadros por segundo | Archivo temporal y su tamaño | `UnsupportedImage` | — |
| Puerto `StickerEncoder.encodeTrayIcon(stickerFile)` | Archivo de sticker | PNG de 96×96 | — | US4.1 |
| `FitCalculator.fit(width, height)` | Tamaño de origen | Tamaño y posición en el lienzo de 512 (BR-06) | — | US3.1 |

`StickerConverter` decide con BR-07 a BR-10 y llama al codificador las veces
necesarias; borra los temporales descartados (NFR17).

## CMP-06 / CMP-07 / CMP-08 — Paquetes

| Operación | Entrada | Salida | Errores | Historias |
|---|---|---|---|---|
| `PackService.addSticker(converted)` | `ConvertedSticker` | Paquete o paquetes modificados (dos si hubo reparto, BR-14) | `StorageError` | US4.1, US4.2, US4.3 |
| `PackService.listPacks()` | — | Paquetes con su `PackStatus` | — | US4.4 |
| `PackService.statusOf(pack)` | Paquete | `PackStatus` (BR-18) | — | US4.1, US4.4 |
| `PackValidator.validate(pack)` | Paquete | Válido o lista de incumplimientos (BR-19) | — | US4.1 |
| Puerto `PackRepository.load()` | — | Todos los paquetes | `StorageError` | US4.4 |
| Puerto `PackRepository.commit(changes)` | Archivos nuevos y paquetes modificados | — (atómico, BR-21) | `StorageError` | US4.1 – US4.3 |
| Puerto `StickerPackPublisher.isWhatsAppInstalled()` | — | Sí o no | — | US4.5 |
| Puerto `StickerPackPublisher.isAdded(packIdentifier)` | Identificador | Sí o no, preguntando a WhatsApp | — | US4.1, US4.4 |
| Puerto `StickerPackPublisher.notifyChanged(pack)` | Paquete | — (WhatsApp relee el paquete) | — | US4.2 |

Abrir la confirmación de alta de WhatsApp necesita una pantalla activa, así
que no es un puerto de `core`: la interfaz recibe la acción `RequestAdd` y
lanza el intent de WhatsApp mediante CMP-08 (`WhatsAppIntents.addPack(pack)`).

### Contrato hacia WhatsApp (`StickerContentProvider`)

Lo define WhatsApp; CMP-08 lo implementa leyendo de CMP-07.

| Consulta | Devuelve |
|---|---|
| `metadata` | Todos los paquetes válidos: identificador, nombre, autor, ícono, versión, si es animado |
| `metadata/<identificador>` | Un paquete |
| `stickers/<identificador>` | Archivos del paquete con sus emojis |
| `stickers_asset/<identificador>/<archivo>` | El archivo del sticker o del ícono |

Solo se exponen paquetes que pasan `PackValidator` (BR-19). El acceso exige
el permiso de lectura que WhatsApp define (NFR8).

## CMP-09 — Guardado

| Operación | Entrada | Salida | Errores | Historias |
|---|---|---|---|---|
| `SaveStickersUseCase.save(sources, onProgress)` | Referencias de las imágenes elegidas o importadas | `SaveResult` con guardados, sin animación, fallidos y una `PackAction` por paquete afectado (BR-20) | `StorageError` si no se pudo guardar nada; los fallos por imagen van dentro del resultado (BR-22) | US3.3, US4.1, US4.2, US5.2 |

## CMP-10 — Interfaz

| Pantalla | Estado principal | Eventos | Historias |
|---|---|---|---|
| Entrada | Texto del enlace, error de enlace | Pegar y buscar, importar imagen, ver paquetes; recibe el intent Compartir | US1.1 – US1.3, US5.2 |
| Búsqueda y cuadrícula | Cargando / imágenes y selección / vacío / error | Marcar, cargar más, guardar stickers, cancelar, reintentar, importar imagen | US2.1 – US2.4, US5.1 |
| Resultado | Avance de la conversión; resumen y acciones pendientes | Confirmar en WhatsApp (automático), volver | US3.3, US4.1, US4.2, US4.5 |
| Paquetes | Lista con estado | Agregar a WhatsApp | US4.4 |

Cada pantalla tiene un ViewModel con un único estado inmutable; la pantalla
envía eventos y pinta el estado.

## CMP-11 — Diagnóstico

| Operación | Entrada | Salida | Historias |
|---|---|---|---|
| Puerto `DiagnosticLog.event(tag, message, cause)` | Etiqueta, mensaje sin datos personales, causa opcional | — | US6.1 |

## Errores y su mensaje al usuario

| Error | Mensaje (resumen) | Acciones |
|---|---|---|
| Enlace no válido | "No es un enlace de video de TikTok" | — |
| `NoConnection` | Sin conexión a internet | Reintentar, Importar imagen |
| `PostUnavailable` | El video no existe o no está disponible | Reintentar, Importar imagen |
| `CommentsBlocked` | TikTok no dejó ver los comentarios | Reintentar, Importar imagen |
| `Timeout` | TikTok tardó demasiado en responder | Reintentar, Importar imagen |
| `UnexpectedFormat` | TikTok cambió algo y la app necesita actualizarse | Reintentar, Importar imagen |
| Sin imágenes | "Este video no tiene imágenes en los comentarios cargados" | Cargar más, Importar imagen |
| Fallo de conversión parcial | "N no se pudo convertir" en el resumen | — |
| `StorageError` | No se pudieron guardar los stickers | Reintentar |
| WhatsApp ausente | "WhatsApp no está instalado" | — |
