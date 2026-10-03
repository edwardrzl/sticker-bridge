# Evaluación de factibilidad

Comprueba si la intención puede entregarse dentro de sus restricciones (un
solo usuario en Android, costo cero, sin fecha límite) y qué riesgos van a
condicionar el diseño. Se basa en la investigación previa y en búsquedas web
del 2026-10-02; nada se ha probado todavía en un dispositivo.

## Veredicto

**Factible con condiciones** — como app Android propia que extrae los stickers
en el mismo teléfono; la vía "web sin instalar nada" no cumple la intención.

## Evaluación

| Dimensión | Nivel | Justificación |
|---|---|---|
| Técnica | Alto riesgo | La extracción depende de una interfaz no oficial de TikTok protegida por firmas (`X-Bogus`, `X-Gnarly`, `msToken`) que exigen un entorno de navegador real y cambian sin aviso. Leerla desde un navegador interno de la app es plausible pero no está probado. La conversión a WebP de 512×512 y la entrega del paquete a WhatsApp usan mecanismos documentados (riesgo bajo). |
| Entrega (tiempo/equipo) | Riesgo medio | Una sola persona, sin fecha límite. El tamaño es pequeño (extraer, elegir, convertir, entregar), pero exige desarrollo Android nativo o híbrido y la experiencia del equipo con ese stack no se ha establecido. |
| Costo | Riesgo bajo | Extracción en el dispositivo: sin servidor, sin proxies, sin servicios de pago. Instalación directa del APK en el propio teléfono: sin cuota de Google Play. Costo de operación esperado: cero. |
| Seguridad / cumplimiento | Riesgo medio | La extracción va contra los términos de servicio de TikTok (aceptado para uso personal). No se usa ni se almacena la sesión de TikTok. Los stickers son contenido de terceros: aceptable para uso privado, sería un problema si se publicara la app. |

### Hallazgos que cambian la investigación previa

- **Confirmado:** la API web de TikTok exige las firmas `X-Bogus` y `X-Gnarly`
  más la cookie `msToken`, que dependen de un entorno JavaScript real.
- **Nuevo:** TikTok filtra las direcciones IP de centros de datos; las
  funciones gratuitas en la nube (Vercel, AWS Lambda) reciben 403 o captcha
  desde la primera petición, y los proxies residenciales son de pago. Un
  servidor gratuito en la nube queda descartado como vía principal.
- **Nuevo:** desde una web no se puede entregar un sticker animado ni un
  paquete a WhatsApp; el creador integrado de WhatsApp en Android solo produce
  stickers estáticos, uno por uno, a partir de una imagen de la galería. La
  recomendación inicial de "empezar con una web" no cumple la animación ni el
  guardado automático.
- **Sin confirmar:** que la lista de comentarios traiga la URL del sticker
  (campo `image_list`) y que los stickers lleguen como WebP animado. Proviene
  de documentación de scrapers de terceros.
- **Discrepancia entre fuentes:** el límite de duración de un sticker animado
  de WhatsApp aparece como 10 s en unas fuentes y ~6 s en otras. Hay que
  verificarlo contra la documentación oficial de WhatsApp al definir requisitos.

## Riesgos principales

| ID | Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|---|
| RSK-01 | El navegador interno de la app no logra leer los stickers de los comentarios (TikTok exige iniciar sesión, muestra captcha o redirige a abrir su app) | Media | Alto: sin extracción no hay producto | Prueba corta antes de diseñar el resto (ver Condiciones). Alternativas: computadora de casa con navegador automatizado, o scraper de pago |
| RSK-02 | TikTok cambia su web o sus firmas y la extracción deja de funcionar | Alta a lo largo del tiempo | Medio: se arregla, uso personal | Aislar la extracción en un componente propio y reemplazable; preferir leer lo que el navegador interno ya cargó en lugar de reimplementar las firmas; mensaje de error claro cuando falle |
| RSK-03 | Stickers animados que no caben en el límite de WhatsApp (500 KB, duración máxima) tras recomprimir | Media | Bajo: ya aceptado | Recomprimir reduciendo calidad y cuadros; si aún no cabe, entregar estático |
| RSK-04 | La conversión de WebP animado en el teléfono es lenta o difícil con las librerías disponibles | Media | Medio: afecta la meta de menos de 1 minuto | Evaluar librerías en Tech Stack Definition; medir en la prueba corta |
| RSK-05 | WhatsApp rechaza el paquete (formato, tamaño, ícono, mínimo de 3 stickers) | Baja | Medio | Validar cada sticker y el paquete contra los límites antes de entregar; completar o avisar si se eligieron menos de 3 |
| RSK-06 | Falta de experiencia en desarrollo Android alarga la entrega | Sin establecer | Medio | Decidir el stack en Tech Stack Definition según lo que la persona ya conoce |
| RSK-07 | Bloqueo o limitación de la IP o del dispositivo por parte de TikTok | Baja (uso personal, bajo volumen, sin sesión) | Medio | Sin automatización masiva: una carga de video por acción del usuario |

## Restricciones confirmadas

- Plataforma: Android; app propia instalada una vez en el teléfono del usuario.
- La extracción ocurre en el dispositivo, con la conexión normal del usuario,
  sin servidor propio y sin entregar la sesión de TikTok.
- Costo de operación cero.
- Sin fecha límite; equipo de una persona.
- Destino: WhatsApp, mediante paquete de stickers (de 3 a 30 stickers, WebP de
  512×512, estático ≤ 100 KB, animado ≤ 500 KB, ícono de 96×96 ≤ 50 KB).
- Meta de éxito vigente: menos de 1 minuto del enlace del video al sticker
  usable en WhatsApp.

## Condiciones para avanzar

1. **Prueba corta de extracción primero.** Antes de construir el resto, una
   prueba en un teléfono Android real debe demostrar que el navegador interno
   carga un video de TikTok y obtiene las URL de los stickers de sus
   comentarios sin iniciar sesión. Debe ser la primera unidad de construcción.
2. **Plan B definido si la prueba falla:** extracción en la computadora de casa
   (gratis, requiere tenerla encendida) o scraper de pago (rompe el costo
   cero). La decisión se toma con la persona en ese momento.
3. **La extracción queda aislada** del resto de la app, para poder cambiarla
   sin tocar la selección, la conversión ni la entrega a WhatsApp.
4. **La declaración de intención se actualiza:** "sin instalar nada" pasa a
   "instalar solo esta app, una vez" (hecho en esta etapa).

## Supuestos y preguntas abiertas

- [assumption] TikTok muestra los comentarios con stickers en su versión web
  sin iniciar sesión. Lo confirma o descarta la prueba corta.
- [assumption] La respuesta de comentarios incluye la URL del archivo del
  sticker y este llega como WebP animado.
- [assumption] La app se instalará directamente en el teléfono (APK), sin
  publicarla en Google Play.
- **Abierta (requisitos):** límite real de duración de los stickers animados
  de WhatsApp (10 s o ~6 s).
- **Abierta (Tech Stack Definition):** experiencia previa de la persona con
  desarrollo Android y preferencia entre nativo e híbrido.
- **Abierta (alcance):** cómo llega el enlace a la app — pegarlo, o recibirlo
  desde el menú Compartir de TikTok.

## Fuentes

- https://github.com/3MH-Technologies/tiktok-internals
- https://github.com/justbeluga/tiktok-web-reverse-engineering
- https://www.npmjs.com/package/tiktok-signature
- https://proxycove.com/en/blog/proxy-for-serverless-applications
- https://www.proxies.sx/anti-bot/tiktok
- https://decodo.com/blog/scrape-tiktok
- https://www.androidpolice.com/whatsapp-sticker-creator-android-beta/
- https://moda.app/resources/sizes/whatsapp-sticker
- https://leapcell.io/blog/how-to-host-playwright-for-free
- `aidlc-docs/investigacion-previa.md`
