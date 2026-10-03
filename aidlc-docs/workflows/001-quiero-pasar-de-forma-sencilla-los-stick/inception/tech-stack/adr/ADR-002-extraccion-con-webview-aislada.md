# ADR-002: Extracción con navegador interno, aislada tras un contrato

## Estado
Aceptada (por delegación de la persona; pendiente de su revisión). Sujeta al
resultado del esqueleto funcional (RSK-01).

## Fecha
2026-10-02

## Contexto
TikTok no ofrece una API oficial para leer comentarios. Su API web exige
peticiones firmadas (`X-Bogus`, `X-Gnarly`) y una cookie `msToken` que solo se
generan en un entorno de navegador real, y filtra las direcciones IP de
centros de datos. El costo debe ser cero y está prohibido usar la sesión de
TikTok del usuario o enviar datos a servidores propios. La regla del proyecto
exige que la extracción sea reemplazable sin tocar el resto.

## Decisión
La extracción se hace en el teléfono con un `WebView` del sistema, oculto y no
interactivo, que carga la página pública de la publicación. Un script inyectado
observa las respuestas de la lista de comentarios que la propia página pide y
las entrega a la app, que extrae de ellas las direcciones de las imágenes. La
app no firma peticiones ni reimplementa algoritmos de TikTok. El `WebView`
bloquea los dominios que no son de TikTok y borra sus cookies y datos al
terminar.

Todo esto vive detrás de un contrato definido en `core/extraction` (pedir
imágenes de una publicación, pedir más, cancelar; resultados y errores con
tipo). El resto de la app solo conoce ese contrato.

## Consecuencias

### Positivas
- Costo cero: sin servidor ni proxies; se usa la conexión normal del usuario.
- Las firmas las genera el propio código de TikTok; un cambio en ellas no nos
  afecta.
- Sustituir la vía de extracción (computadora de casa, scraper de pago) es
  escribir otra implementación del contrato.

### Negativas
- Depende de la estructura de la página y de la forma de sus respuestas:
  TikTok puede romperla sin aviso (RSK-02).
- No se puede probar automáticamente contra TikTok real; solo la lectura de
  respuestas con ejemplos guardados y la comprobación manual.
- TikTok puede exigir sesión, verificación o redirigir a su app (RSK-01).

### Neutras
- Para pedir más comentarios hay que provocar que la página los pida
  (desplazamiento simulado), no llamar a la API directamente.

## Alternativas consideradas

### Llamar a la API web firmando en código propio
- Pros: más rápido, sin `WebView`.
- Contras: hay que seguir los cambios de firma; TikTok descarta el tráfico que
  no procede de un navegador aunque esté firmado.

### Navegador automatizado en un servidor en la nube
- Contras: IP de centro de datos bloqueada; proxies residenciales de pago.

### Scraper de pago (Apify)
- Pros: más estable.
- Contras: rompe el costo cero y envía datos a terceros. Queda como plan B.

### Servidor en la computadora de casa
- Pros: gratis, IP residencial.
- Contras: debe estar encendida; añade un segundo sistema. Queda como plan B.

## Referencias
- `feasibility-assessment.md` (RSK-01, RSK-02, condiciones 1–3)
- `requirements.md` (FR2, FR6, NFR4, NFR6, NFR11)
