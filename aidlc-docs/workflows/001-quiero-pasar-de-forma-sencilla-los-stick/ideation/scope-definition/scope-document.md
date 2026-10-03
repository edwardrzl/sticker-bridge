# Documento de alcance

Frontera de este workflow: qué capacidades entran en la primera versión de la
app, cuáles quedan fuera y por qué, los límites de plataforma e integraciones,
y qué debe cumplirse para darla por terminada.

## Resumen

Primera versión de una app Android de uso personal. Recibe un video de TikTok
(compartido o pegado), extrae en el propio teléfono las imágenes de sus
comentarios, deja elegir las que se quieren, las convierte al formato de
stickers de WhatsApp y las suma a un paquete propio que se agrega a WhatsApp de
un toque. Incluye una vía manual de respaldo para cuando la extracción falle.

Son 10 capacidades dentro del alcance (7 Must, 3 Should) y 3 aplazadas a
"Futuro". Priorización: MoSCoW.

## Dentro del alcance

| ID | Capacidad | Valor | Tamaño | Prioridad (MoSCoW) |
|---|---|---|---|---|
| CAP-01 | Extraer en el teléfono las imágenes de los comentarios de un video (stickers y fotos), sin iniciar sesión en TikTok | Sin esto no hay producto; es el riesgo principal (RSK-01) | L | Must |
| CAP-02 | Recibir el video desde el botón Compartir de TikTok | Es el camino más corto hacia la meta de menos de 1 minuto | S | Must |
| CAP-03 | Recibir el video pegando el enlace | Respaldo cuando Compartir no esté disponible | S | Must |
| CAP-04 | Cuadrícula de imágenes encontradas con selección por toque | Elegir solo lo que se quiere | M | Must |
| CAP-05 | Convertir cada imagen elegida a sticker de WhatsApp: WebP de 512×512 dentro del límite de peso, sin intervención manual | Elimina el recorte a mano, que es el problema de hoy | M | Must |
| CAP-06 | Paquetes propios guardados en el teléfono que van creciendo, en dos series (animados y estáticos, porque WhatsApp no permite mezclarlos); al llenarse uno de 30 se abre otro; se agregan o actualizan en WhatsApp de un toque | Permite guardar un solo sticker por video una vez que la serie tiene su primer paquete de 3; cumple "que me la guarde solita" | M | Must |
| CAP-07 | Mensajes claros cuando la extracción falla (sin conexión, video sin imágenes en comentarios, TikTok bloquea o cambió) | La extracción es frágil por naturaleza; el usuario debe saber qué pasó y qué hacer | S | Must |
| CAP-08 | Conservar la animación cuando el sticker cabe en los límites de WhatsApp; si no cabe, entregarlo estático | Los stickers de TikTok son animados; la captura de pantalla la pierde | M | Should |
| CAP-09 | Botón "cargar más" para traer más tandas de comentarios | Llegar a stickers que no están en los primeros comentarios | S | Should |
| CAP-10 | Plan B manual: importar una imagen desde la galería y pasarla por la misma conversión y el mismo paquete | Mantiene la app útil cuando la extracción se rompe (RSK-02) | S | Should |

CAP-08 es Should porque la persona la calificó de "deseable" y aceptó la caída
a estático; CAP-09 y CAP-10 lo son porque el flujo principal funciona sin ellas.

## Fuera del alcance (y por qué)

| Capacidad | Por qué |
|---|---|
| Quitar stickers ya guardados en el paquete | Aplazada a Futuro por decisión de la persona. Consecuencia aceptada: un sticker agregado por error permanece en el paquete |
| Evitar agregar dos veces el mismo sticker | Aplazada a Futuro |
| Vista previa animada en la cuadrícula | Aplazada a Futuro; en la cuadrícula se ven estáticos |
| Herramienta de recorte o edición manual (recortar a gusto, añadir texto) | No-objetivo de la intención. Consecuencia aceptada: el plan B manual ajusta la imagen completa, una captura de pantalla saldría con la interfaz alrededor |
| Stickers favoritos guardados en la cuenta de TikTok | No-objetivo: exigiría entregar la sesión de TikTok |
| Otros destinos (Telegram, Discord) | No-objetivo |
| Cuentas de usuario, sincronización o biblioteca en la nube | No-objetivo; el paquete vive solo en el teléfono |
| iPhone | No-objetivo; el usuario usa Android |
| Web o PWA | Descartada en factibilidad: no puede entregar animados ni paquetes |
| Servidor propio, proxies o scrapers de pago | Descartados en factibilidad: la extracción es en el dispositivo y a costo cero. Solo se reconsideran si falla la prueba de extracción |
| Publicación en Google Play | Uso personal; instalación directa [assumption] |
| Recorrer todos los comentarios automáticamente | Más lento y más riesgo de bloqueo; se cubre con "cargar más" |

## Límites (plataformas, roles, integraciones)

- **Plataforma:** solo Android, un único teléfono (el del usuario). La versión
  mínima de Android se fija en Tech Stack Definition.
- **Idioma de la interfaz:** español [assumption].
- **Roles:** un único usuario, sin registro ni permisos diferenciados.
- **Integraciones reales (no simuladas):**
  - TikTok, mediante su versión web cargada en un navegador interno de la app.
    Interfaz no oficial, sin sesión del usuario.
  - WhatsApp, mediante su mecanismo oficial de paquetes de stickers para apps
    Android.
- **Datos:** los stickers convertidos y los paquetes se guardan solo en el
  teléfono. No se envía nada a servidores propios.
- **Volumen:** uso personal; una carga de video por acción del usuario, sin
  automatización masiva.

## Criterios de salida (release)

La versión se da por terminada cuando, en el teléfono del usuario:

1. Con **5 videos reales distintos** de TikTok que tengan imágenes en sus
   comentarios, en cada uno se llega del enlace al sticker visible y usable en
   WhatsApp en **menos de 1 minuto**.
2. Ningún paso del flujo pide recortar o ajustar la imagen a mano.
3. El video puede darse tanto desde Compartir de TikTok como pegando el enlace.
4. Con una serie que ya tiene su primer paquete agregado, un solo sticker
   elegido de un video termina disponible en WhatsApp, incluido el paso al
   segundo paquete al superar 30. Los 2 primeros stickers de cada serie
   esperan al tercero (actualizado en Requirements Analysis: WhatsApp exige 3
   por paquete y no permite mezclar animados con estáticos).
5. Al menos un sticker animado de origen sigue animado en WhatsApp, y uno que
   no cabe en el límite se entrega estático sin error.
6. Con la extracción fallando (por ejemplo, sin conexión), la app muestra un
   mensaje comprensible y el plan B manual permite agregar una imagen de la
   galería.
7. Todas las capacidades Must están completas y sus pruebas pasan.

## Supuestos y preguntas abiertas

- [assumption] La app se instala directamente en el teléfono, sin Google Play.
- [assumption] La interfaz está en español.
- [assumption] WhatsApp permite actualizar un paquete ya agregado cuando la app
  le suma stickers. Si exige volver a agregarlo, CAP-06 lo hará en un toque.
  A confirmar en diseño.
- [assumption] No siempre será posible distinguir un sticker de una foto en un
  comentario; por eso se muestran todas las imágenes.
- **Abierta (requisitos):** qué hacer con los paquetes que aún tienen menos de
  3 stickers (el primero, y cada paquete nuevo tras llegar a 30): WhatsApp no
  los acepta hasta tener 3.
- **Abierta (requisitos):** cuántas tandas de comentarios se cargan al inicio.
- **Abierta (requisitos):** límite real de duración de los stickers animados
  de WhatsApp (10 s o ~6 s según la fuente).
- **Heredada de factibilidad:** la prueba de extracción en un teléfono real
  (RSK-01) debe ser lo primero que se construya.
