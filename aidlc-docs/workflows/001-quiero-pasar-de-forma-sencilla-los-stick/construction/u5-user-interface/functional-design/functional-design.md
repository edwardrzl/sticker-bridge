# Diseño funcional — U5 — Interfaz completa

Las pantallas definitivas, que sustituyen a la pantalla de prueba: recibir el
video, ver y **elegir** sus stickers ordenados por likes, guardarlos con una
confirmación de WhatsApp por paquete, importar de la galería, y ver los paquetes
pudiendo **quitar** stickers. Textos en español.

## Alcance de la unidad

Historias: US1.1, US1.2, US2.2, US2.4, US4.4, US5.1 (presentación), US5.2 y
CAP-11 · Requisitos: FR1.1, FR1.2, FR2.3, FR2.4, FR3.1–FR3.4, FR5.8, FR5.10,
FR5.11, FR6.1–FR6.3, FR7.1–FR7.3 · NFR3, NFR13–NFR15.

## Pantallas

Una sola actividad con tres pantallas, sin librería de navegación (estado en un
`ViewModel` raíz). Atrás vuelve a Entrada.

### P1 — Entrada

| Elemento | Comportamiento |
|---|---|
| Campo "Enlace del video" + "Buscar stickers" | Valida con `PostLinkParser`; si no es válido, "No es un enlace de video de TikTok" bajo el campo (FR1.4) |
| Compartir desde TikTok | La app aparece en el menú Compartir para texto; al recibirlo, rellena el campo y busca sola (FR1.1) |
| "Importar imagen" | Selector de fotos del sistema, varias a la vez; las elegidas se guardan como en P2 (FR7) |
| "Mis paquetes" | Abre P3 |

### P2 — Stickers del video

| Estado | Contenido |
|---|---|
| Buscando | Indicador y "Cancelar" (FR2.3) |
| Lista | Cuadrícula de miniaturas (primer cuadro, ≥ 96 dp) ordenada por likes, con el número de likes en cada una. Tocar marca o desmarca; contador "N elegidos" (FR3.1, FR3.2). "Cargar más" al final si hay más comentarios (FR2.4). Botón "Guardar stickers", desactivado sin selección (FR3.3) |
| Sin imágenes | "Este video no tiene imágenes en los comentarios cargados" con "Cargar más" e "Importar imagen" (FR3.4) |
| Error | Mensaje según el tipo (tabla de `interfaces.md`) con "Reintentar" e "Importar imagen" (FR6.1, FR6.2) |
| Guardando | Progreso "Convirtiendo 2 de 5" |
| Resumen | Cuántos se guardaron y dónde, cuántos sin animación, cuántos fallaron, qué paquetes esperan stickers; WhatsApp se abre solo, una vez por paquete afectado (FR5.5, FR5.9, FR5.10) |

### P3 — Mis paquetes

| Elemento | Comportamiento |
|---|---|
| Lista de paquetes | Nombre, tipo, número de stickers, estado: "faltan N", "listo para agregar", "agregado" (FR5.8) |
| Miniaturas del paquete | Sus stickers; tocar uno pregunta "¿Quitar este sticker?" (FR5.11) |
| "Agregar a WhatsApp" / "Actualizar en WhatsApp" | Abre la pantalla de WhatsApp para ese paquete |
| Tras quitar | Si el paquete está agregado, se abre la pantalla de WhatsApp para actualizarlo |

## Lógica

- `MainViewModel`: pantalla actual y enlace recibido por Compartir.
- `SearchViewModel`: sesión de extracción, imágenes, selección (conjunto de URL),
  cargar más, guardar con `SaveStickersUseCase`, cola de confirmaciones de WhatsApp.
- `PacksViewModel`: paquetes con su estado (pregunta a WhatsApp, ADR-006), quitar.
- Miniaturas: cargador propio con la misma descarga que la conversión (cabecera
  `Referer` de TikTok) y caché en memoria; las de los paquetes se leen del disco.
- Errores: mensaje por tipo de `ExtractionError`; nada cierra la app (FR6.4).

## Casos de prueba derivados

Automáticos (ViewModels con puertos falsos, JVM):

1. Un enlace inválido no abre sesión y muestra el error.
2. Tras cargar, la selección empieza vacía; marcar dos y desmarcar uno deja uno.
3. "Guardar" sin selección no hace nada; con selección guarda solo los elegidos, en
   orden de likes.
4. Cargar más conserva la selección.
5. Un fallo de extracción muestra su mensaje y permite reintentar.
6. Tras guardar, cada `OpenWhatsApp` se presenta una vez, en orden.
7. Paquetes: el estado combina número de stickers y lo que dice WhatsApp; quitar
   un sticker de un paquete agregado pide confirmación de WhatsApp.

Manuales (teléfono): el criterio de salida del alcance (5 videos reales, < 1 min,
sin recorte), Compartir desde TikTok, importación, quitar un sticker.

## Trazabilidad

| Historia / FR | Sección |
|---|---|
| US1.1, US1.2 · FR1.1, FR1.2, FR1.4 | P1, caso 1 |
| US2.2, US2.4 · FR3.1–FR3.4 | P2, casos 2–4 |
| US5.1 · FR6.1–FR6.3 | P2 (error), caso 5 |
| US3.3, US4.1, US4.2 · FR5.5, FR5.9, FR5.10 | P2 (resumen), caso 6 |
| US4.4, CAP-11 · FR5.8, FR5.11 | P3, caso 7 |
| US5.2 · FR7.1–FR7.3 | P1 |
