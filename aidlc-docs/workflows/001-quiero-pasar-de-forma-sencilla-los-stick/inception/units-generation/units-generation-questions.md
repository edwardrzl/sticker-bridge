# Preguntas — Units Generation

> Responde cada pregunta escribiendo la letra (y, si quieres, un comentario)
> después de `[Answer]:`. Para varias opciones: `[Answer]: A, C`.
> Si ninguna opción encaja, usa `X` y explica.

> **Respuestas delegadas.** La persona pidió avanzar sin consultarla. Las
> respuestas son la opción recomendada, elegida por Claude.

## Q1 — Cómo se cortan las unidades
Cada unidad se construye, prueba y une a `main` en su propia rama y pull request.
¿Con qué criterio se agrupan los componentes?

A. Un esqueleto funcional de punta a punta primero, y después una unidad por funcionalidad completa: extracción, conversión, paquetes y WhatsApp, interfaz (recomendada — cumple la decisión de esqueleto primero y cada unidad tiene sentido por sí sola)
B. Una unidad por componente (11 unidades)
C. Por capas: todo `core` y luego todo `app`
X. Otra (especifica)

[Answer]: A — Esqueleto y luego por funcionalidad (delegada)

## Q2 — Tamaño de las unidades
Una unidad debería poder terminarse en una sesión de trabajo, de horas a pocos días.
¿Cuántas unidades?

A. Cinco unidades de tamaño medio (recomendada — cada pull request se puede revisar de una vez)
B. Dos o tres unidades grandes
C. Diez o más unidades pequeñas
X. Otra (especifica)

[Answer]: A — Cinco unidades (delegada)

## Q3 — Qué se entrega
Define si alguna unidad produce algo instalable por separado.
¿Cómo se despliega?

A. Un único APK; todas las unidades aportan al mismo (recomendada — es una sola app en un solo teléfono)
B. Varias apps
X. Otra (especifica)

[Answer]: A — Un único APK (delegada)
