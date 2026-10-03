# Backlog

Capacidades ordenadas por prioridad (MoSCoW) y, dentro de cada nivel, por el
orden en que conviene abordarlas. Las aplazadas están al final, bajo "Futuro".
Los detalles de cada una están en `scope-document.md`.

## Must

| Orden | ID | Capacidad | Tamaño | Nota |
|---|---|---|---|---|
| 1 | CAP-01 | Extraer en el teléfono las imágenes de los comentarios de un video | L | Primero: es la prueba que factibilidad exige (RSK-01). Si falla, se replantea la vía antes de seguir |
| 2 | CAP-05 | Convertir cada imagen elegida a sticker de WhatsApp (WebP 512×512, dentro del peso) | M | |
| 3 | CAP-06 | Paquete propio que va creciendo y se agrega a WhatsApp de un toque | M | |
| 4 | CAP-04 | Cuadrícula de imágenes con selección por toque | M | |
| 5 | CAP-03 | Recibir el video pegando el enlace | S | |
| 6 | CAP-02 | Recibir el video desde Compartir de TikTok | S | |
| 7 | CAP-07 | Mensajes claros cuando la extracción falla | S | |

## Should

| Orden | ID | Capacidad | Tamaño | Nota |
|---|---|---|---|---|
| 8 | CAP-08 | Conservar la animación cuando cabe; estático si no | M | Depende de CAP-05 |
| 9 | CAP-10 | Plan B manual: importar una imagen desde la galería | S | Reutiliza CAP-05 y CAP-06 |
| 10 | CAP-09 | Botón "cargar más" comentarios | S | Depende de CAP-01 |

## Futuro

| Capacidad | Por qué se aplaza |
|---|---|
| Quitar stickers ya guardados en el paquete | No elegida para esta versión |
| Evitar agregar dos veces el mismo sticker | No elegida para esta versión |
| Vista previa animada en la cuadrícula | No elegida para esta versión |
| Abrir la app a otras personas | Hoy es de uso personal; implicaría revisar el riesgo con los términos de TikTok y la distribución |
