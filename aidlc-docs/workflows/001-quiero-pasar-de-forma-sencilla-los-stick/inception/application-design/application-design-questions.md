# Preguntas — Application Design

> Responde cada pregunta escribiendo la letra (y, si quieres, un comentario)
> después de `[Answer]:`. Para varias opciones: `[Answer]: A, C`.
> Si ninguna opción encaja, usa `X` y explica.

> **Respuestas delegadas.** La persona pidió avanzar sin consultarla. Las
> respuestas son la opción recomendada, elegida por Claude.

## Q1 — Estilo de arquitectura
Define dónde viven las reglas y cómo se aíslan TikTok, WhatsApp y la librería de imágenes.
¿Qué estilo se sigue?

A. Puertos y adaptadores: `core` contiene las reglas y define interfaces (puertos) para todo lo externo; `app` las implementa (adaptadores) (recomendada — es la forma directa de cumplir "extracción reemplazable" y "lógica sin Android", y se ve clara en un repositorio de muestra)
B. Capas clásicas (interfaz, dominio, datos) dentro de un solo módulo
C. Sin capas: pantallas que llaman directamente a WebView, archivos y WhatsApp
X. Otra (especifica)

[Answer]: A — Puertos y adaptadores (delegada)

## Q2 — Cómo sabe la app si un paquete ya está en WhatsApp
De ello depende si tras guardar se actualiza el paquete o se abre la confirmación de WhatsApp.
¿De dónde sale ese dato?

A. Se pregunta a WhatsApp cada vez, con la consulta que WhatsApp ofrece para eso (recomendada — es la única fuente fiable: el usuario puede quitar el paquete desde WhatsApp sin que la app se entere)
B. La app lo anota cuando WhatsApp confirma el alta
X. Otra (especifica)

[Answer]: A — Se pregunta a WhatsApp cada vez (delegada)

## Q3 — Conversión de varias imágenes
Afecta al tiempo total y al uso de memoria en el teléfono.
¿Cómo se convierten cuando se eligen varias?

A. Una tras otra, mostrando el avance (recomendada — una animación de 512×512 ocupa mucha memoria; en serie cabe en el presupuesto de 20 s para 3 stickers)
B. Varias a la vez
X. Otra (especifica)

[Answer]: A — Una tras otra (delegada)

## Q4 — Estado de las pantallas
Define cómo se comunican pantallas y lógica.
¿Qué patrón se usa?

A. Un ViewModel por pantalla con un único estado inmutable y eventos hacia abajo (flujo unidireccional) (recomendada — es el patrón estándar con Compose y hace el estado fácil de razonar)
B. Un único ViewModel para toda la app
X. Otra (especifica)

[Answer]: A — Un ViewModel por pantalla, flujo unidireccional (delegada)
