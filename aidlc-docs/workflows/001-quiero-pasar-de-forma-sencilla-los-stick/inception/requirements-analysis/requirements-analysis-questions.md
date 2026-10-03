# Preguntas — Requirements Analysis

> Responde cada pregunta escribiendo la letra (y, si quieres, un comentario)
> después de `[Answer]:`. Para varias opciones: `[Answer]: A, C`.
> Si ninguna opción encaja, usa `X` y explica.

> **Respuestas delegadas.** La persona pidió avanzar sin consultarla ("avanza
> con las opciones recomendadas", "las aprobaciones también hazlas por tu
> cuenta"). Todas las respuestas de este archivo son la opción recomendada,
> elegida por Claude. Para cambiar alguna, edita la respuesta y pide revisar
> los requisitos.

## Q1 — Paquetes con menos de 3 stickers
WhatsApp no acepta un paquete con menos de 3 stickers, así que los dos primeros stickers de cada paquete nuevo no pueden aparecer en WhatsApp por sí solos.
¿Qué hace la app mientras un paquete tiene 1 o 2 stickers?

A. Los guarda en el paquete y muestra cuántos faltan para poder agregarlo a WhatsApp; al llegar a 3 ofrece agregarlo (recomendada — es lo más simple y no mete stickers falsos en WhatsApp)
B. Completa el paquete con stickers de relleno temporales que se sustituyen al llegar los reales
X. Otra (especifica)

[Answer]: A — Guardar y mostrar cuántos faltan (delegada)

## Q2 — Animados y estáticos no pueden mezclarse
La documentación oficial de WhatsApp indica que un paquete contiene solo stickers estáticos o solo animados, nunca ambos. El alcance preveía un único paquete que crece.
¿Cómo se organizan los paquetes?

A. Dos series de paquetes que crecen por separado: una de animados y otra de estáticos; cada sticker va a la serie que le corresponde (recomendada — respeta la regla de WhatsApp sin trucos)
B. Una sola serie de animados, convirtiendo los estáticos en animaciones de imagen fija
C. Una sola serie de estáticos, renunciando a la animación
X. Otra (especifica)

[Answer]: A — Dos series separadas, animados y estáticos (delegada)

## Q3 — Cuánto carga la app al inicio
El alcance fijó "primeras tandas y botón cargar más", sin número.
¿Cuándo se detiene la carga inicial de comentarios?

A. Tras 3 tandas de comentarios o 10 segundos, lo que ocurra primero; se ajusta tras la prueba en el teléfono (recomendada — acota la espera frente a la meta de 1 minuto)
B. Solo la primera tanda
C. Hasta encontrar al menos 12 imágenes
X. Otra (especifica)

[Answer]: A — 3 tandas o 10 segundos (delegada)

## Q4 — Imágenes que no son cuadradas
Un sticker de WhatsApp es de 512×512 exactos.
¿Cómo se ajusta una imagen que no es cuadrada?

A. Se muestra completa, centrada, con fondo transparente en lo que sobra (recomendada — no se pierde nada de la imagen y no hay que recortar)
B. Se recorta al centro hasta llenar el cuadrado
X. Otra (especifica)

[Answer]: A — Completa, centrada, fondo transparente (delegada)

## Q5 — Nombre de los paquetes y emoji de cada sticker
WhatsApp exige un nombre de paquete y entre 1 y 3 emojis por sticker.
¿Cómo se asignan?

A. Automáticos: paquetes llamados "TikTok animados 1", "TikTok estáticos 1", etc., y un emoji fijo por defecto para todos los stickers (recomendada — no añade pasos al flujo)
B. La app pregunta el nombre al crear cada paquete
C. La app pide elegir emoji para cada sticker
X. Otra (especifica)

[Answer]: A — Automáticos (delegada)

## Q6 — WhatsApp Business
WhatsApp y WhatsApp Business son apps distintas y se integran por separado.
¿A cuál se agregan los paquetes?

A. Solo a WhatsApp normal (recomendada — es el destino declarado en la intención)
B. A cualquiera de las dos que esté instalada
X. Otra (especifica)

[Answer]: A — Solo WhatsApp normal (delegada)

## Q7 — Un sticker ya está en el paquete
Evitar duplicados quedó para una versión futura.
Si se vuelve a elegir un sticker que ya se guardó, ¿qué ocurre?

A. Se agrega otra vez, sin comprobación (recomendada — es lo que implica haber aplazado "evitar duplicados")
B. Se avisa pero se permite
X. Otra (especifica)

[Answer]: A — Se agrega otra vez (delegada)
