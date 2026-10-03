# Preguntas — Feasibility

> Responde cada pregunta escribiendo la letra (y, si quieres, un comentario)
> después de `[Answer]:`. Para varias opciones: `[Answer]: A, C`.
> Si ninguna opción encaja, usa `X` y explica.

## Q1 — "Sin instalar nada" frente a animación y guardado automático
Lo que encontré: desde una web solo se puede entregar el archivo convertido; en WhatsApp para Android el creador de stickers integrado toma una imagen de la galería y produce un sticker estático, uno por uno. Los stickers animados y el paquete completo de un toque solo entran a WhatsApp mediante una app Android. Es decir, la meta "sin instalar nada" (respuesta a la pregunta de éxito, Q4 de Intent Capture) choca con "deseable conservar la animación" (Q5) y con "que me la guarde solita" (Q2).
¿Qué priorizas?

A. App Android propia desde el inicio: se instala una sola vez, conserva la animación y agrega el paquete a WhatsApp de un toque (recomendada — es la única vía que cumple animación y guardado automático, y además resuelve la extracción a costo cero, ver Q2)
B. Web primero, sin instalar: stickers estáticos agregados uno por uno con el creador de WhatsApp; la app Android queda para una versión posterior
C. Solo web, solo estáticos: renuncio a la animación y al paquete de un toque
X. Otra (especifica)

[Answer]: A — App Android propia desde el inicio

## Q2 — Dónde corre la extracción con costo cero
Lo que encontré: TikTok exige un navegador real para firmar las peticiones y bloquea de entrada las direcciones IP de centros de datos (los servidores gratuitos como Vercel o AWS Lambda reciben error 403 o captcha). Los proxies residenciales que lo evitan son de pago. Por eso "servidor gratuito en la nube" tiene una probabilidad alta de no funcionar.
¿Desde dónde quieres que se haga la extracción?

A. En el propio teléfono: la app Android abre el video de TikTok en un navegador interno y lee los stickers de los comentarios; usa tu conexión normal, sin servidor y sin costo (recomendada — solo es posible con la opción A de Q1)
B. En tu computadora de casa: un pequeño servidor propio con navegador automatizado; gratis, pero la computadora debe estar encendida cuando lo uses
C. Servidor gratuito en la nube, aceptando que puede quedar bloqueado y haya que cambiar de vía
D. Scraper de pago (Apify u otro): más estable, rompe la restricción de costo cero (unos pocos dólares según uso)
X. Otra (especifica)

[Answer]: A — En el propio teléfono, con un navegador interno de la app
