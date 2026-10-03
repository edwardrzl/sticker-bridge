# Investigación previa (2026-10-02)

Notas reunidas antes de iniciar el workflow. Vienen de búsquedas web, no de
pruebas propias: la etapa de factibilidad debe confirmarlas.

## Idea

Pasar de forma sencilla los stickers que se usan en los comentarios de TikTok
a stickers de WhatsApp. Alcance sugerido para empezar: `poc`.

## Extracción desde TikTok

- Cada sticker de comentario es un archivo en el CDN de TikTok. La lista de
  comentarios de un video trae su URL (campo `image_list` en los scrapers).
- Los stickers llegan como WebP animado; las fotos, como JPEG.
- No hay API oficial. La API web exige peticiones firmadas (`X-Bogus`,
  `X-Gnarly`, `msToken`): hace falta un navegador automatizado en un servidor
  o un scraper de terceros (Apify).
- Es frágil (TikTok cambia la firma cuando quiere) y va contra sus términos.
- Herramientas que ya lo hacen: ZocialComment (enlace del video → `.zip` con
  todas las imágenes de los comentarios), scrapers de Apify, Stick.it (captura
  de pantalla, no el archivo original).

## Selección de stickers

- Los favoritos guardados en la cuenta no son accesibles sin entregar la sesión
  de TikTok a la herramienta. Descartado.
- Flujo propuesto: copiar el enlace del video → pegarlo o compartirlo a la
  herramienta → cuadrícula con los stickers de sus comentarios → tocar los que
  se quieren → enviarlos a WhatsApp.

## Destino: WhatsApp

- Sticker: WebP de exactamente 512×512. Estático ≤ 100 KB; animado ≤ 500 KB y
  ≤ 10 s. Paquete: de 3 a 30 stickers, ícono de 96×96 ≤ 50 KB.
- Web: entrega los archivos ya convertidos; se agregan uno por uno con el
  creador de stickers de WhatsApp. Puede instalarse como PWA y recibir enlaces
  desde el menú Compartir de Android (Web Share Target).
- App Android: única forma de agregar un paquete completo de un toque
  (intent `com.whatsapp.intent.action.ENABLE_STICKER_PACK` + `contents.json`).
- iPhone: la app nativa exige cuenta de desarrollador de Apple.

## Recomendación inicial

Empezar con una web para validar la extracción, que es lo riesgoso. Pasar a
app Android solo si agregar los stickers uno por uno resulta incómodo.

## Pendiente por definir

- ¿Android o iPhone?
- ¿Navegador automatizado propio o scraper de pago para la extracción?

## Fuentes

- https://zocialcomment.com/blog/how-to-save-pictures-from-tiktok-comments
- https://apify.com/rainminer/tiktok-comments-images-scraper
- https://github.com/carcabot/tiktok-signature
- https://sticko.app/guides/whatsapp-sticker-size-and-format
- https://developer.chrome.com/docs/capabilities/web-apis/web-share-target
- https://play.google.com/store/apps/details?id=com.stickercapture.app
