# Preguntas — Scope Definition

> Responde cada pregunta escribiendo la letra (y, si quieres, un comentario)
> después de `[Answer]:`. Para varias opciones: `[Answer]: A, C`.
> Si ninguna opción encaja, usa `X` y explica.

Capacidades que doy por incluidas (núcleo, sin ellas no se cumple la
intención): extraer en el teléfono los stickers de los comentarios de un video,
mostrarlos en una cuadrícula para elegir, convertirlos al formato de WhatsApp
(conservando la animación cuando cabe) y agregarlos a WhatsApp como paquete de
un toque. Las preguntas cubren lo que no está decidido.

## Q1 — Cómo llega el enlace del video a la app
Define el primer paso del flujo y pesa en la meta de menos de 1 minuto.
¿Cómo quieres darle el video a la app?

A. Desde el botón Compartir de TikTok, eligiendo la app, y también pegando el enlace a mano (recomendada — compartir es lo más rápido y pegar sirve de respaldo)
B. Solo pegando el enlace
C. Solo desde el botón Compartir de TikTok
X. Otra (especifica)

[Answer]: A — Desde el botón Compartir de TikTok y también pegando el enlace

## Q2 — El mínimo de 3 stickers por paquete
WhatsApp solo acepta paquetes de 3 a 30 stickers. Si de un video solo quieres uno, no se puede entregar como paquete por sí solo.
¿Cómo debe comportarse la app?

A. Un paquete propio guardado en el teléfono que va creciendo: cada sticker elegido se suma y WhatsApp se actualiza; al llegar a 30 se abre otro paquete (recomendada — permite guardar un solo sticker por video; es almacenamiento local, no una biblioteca en la nube)
B. Cada video genera un paquete nuevo y la app exige elegir al menos 3
C. Cada video genera un paquete nuevo y, si eliges menos de 3, la app lo completa con stickers de relleno
X. Otra (especifica)

[Answer]: A — Un paquete propio guardado en el teléfono que va creciendo; al llegar a 30 se abre otro

## Q3 — Cuántos comentarios revisar
Un video puede tener miles de comentarios y TikTok los carga por tandas; revisar más tarda más y aumenta el riesgo de bloqueo.
¿Hasta dónde debe buscar la app?

A. Muestra los stickers de las primeras tandas y ofrece un botón "cargar más" (recomendada — rápido por defecto y tú decides cuándo seguir)
B. Recorre todos los comentarios automáticamente antes de mostrar nada
C. Solo las primeras tandas, sin opción de cargar más
X. Otra (especifica)

[Answer]: A — Primeras tandas y botón "cargar más"

## Q4 — Fotos en los comentarios
Además de stickers, los comentarios de TikTok pueden llevar fotos normales (JPEG).
¿Las quieres también como candidatas a sticker?

A. Sí: todo lo que sea imagen en un comentario aparece en la cuadrícula; las fotos se ajustan a 512×512 automáticamente (recomendada — puede que no sea posible distinguir de forma fiable un sticker de una foto)
B. No: solo stickers; las fotos se descartan si se pueden distinguir
X. Otra (especifica)

[Answer]: A — Sí: toda imagen de un comentario aparece en la cuadrícula

## Q5 — Extras de esta primera versión (elige todas las que apliquen)
Estas capacidades no son imprescindibles para cumplir la intención; las que no elijas pasan a "Futuro".
¿Cuáles quieres ya en esta versión?

A. Gestionar el paquete: quitar stickers ya guardados
B. Evitar duplicados: no agregar dos veces el mismo sticker
C. Plan B manual: importar una imagen o captura desde la galería cuando la extracción falle
D. Vista previa animada en la cuadrícula (si no, se ven estáticos hasta elegirlos)
X. Otra (especifica)

[Answer]: C — Plan B manual: importar una imagen desde la galería. A, B y D pasan a "Futuro".

## Q6 — Cuándo damos la versión por terminada
Un criterio de salida observable evita pulir sin fin.
¿Qué debe cumplirse?

A. En tu teléfono, con 5 videos reales distintos: en cada uno, del enlace al sticker visible en WhatsApp en menos de 1 minuto, sin pasos manuales de recorte (recomendada — verifica la métrica de éxito con variedad suficiente)
B. Lo mismo, pero basta con que funcione con 1 video
C. Lo mismo con 5 videos, y además que la app lleve una semana de uso normal sin fallar
X. Otra (especifica)

[Answer]: A — 5 videos reales distintos, menos de 1 minuto cada uno, sin recorte manual
