# Historias de usuario

Los requisitos vistos desde quien usa la app, organizados por su recorrido:
dar el video, ver y elegir, convertir, tenerlos en WhatsApp y qué pasa cuando
algo falla. Son 6 épicas y 17 historias; cada requisito funcional queda
cubierto por al menos una. Las personas están en `personas.md`.

## Épica E1 — Dar el video a la app

### US1.1 — Compartir desde TikTok
**Como** usuario de stickers **quiero** elegir la app desde el botón Compartir
de TikTok **para** empezar sin copiar ni pegar nada.
- **Requisitos:** FR1.1, FR1.3
- **Prioridad:** Must
- **Tamaño:** S

**Criterios de aceptación**
- Dado un video abierto en TikTok, cuando toco Compartir y elijo la app,
  entonces la app se abre y empieza a buscar las imágenes de ese video.
- Dado que TikTok comparte un texto con el enlace dentro, cuando la app lo
  recibe, entonces usa solo el enlace.

### US1.2 — Pegar el enlace
**Como** usuario de stickers **quiero** pegar el enlace de un video **para**
usar la app cuando no puedo compartir directamente.
- **Requisitos:** FR1.2, FR1.3
- **Prioridad:** Must
- **Tamaño:** S

**Criterios de aceptación**
- Dado un enlace de TikTok en cualquiera de sus formas (largo, corto, de
  foto), cuando lo pego y confirmo, entonces empieza la búsqueda.

### US1.3 — Enlace que no sirve
**Como** usuario de stickers **quiero** que la app me avise si lo que le di no
es un video de TikTok **para** no esperar una búsqueda que no va a funcionar.
- **Requisitos:** FR1.4
- **Prioridad:** Must
- **Tamaño:** S

**Criterios de aceptación**
- Dado un enlace de otro sitio, un texto sin enlace o un perfil de TikTok,
  cuando lo doy a la app, entonces veo "No es un enlace de video de TikTok" y
  sigo en la pantalla de entrada.

## Épica E2 — Ver y elegir

### US2.1 — Ver las imágenes de los comentarios
**Como** usuario de stickers **quiero** ver en una cuadrícula las imágenes de
los comentarios del video **para** encontrar el sticker que me gustó.
- **Requisitos:** FR2.1, FR2.2, FR2.3, FR2.5, FR2.6, FR2.7, FR3.1
- **Prioridad:** Must
- **Tamaño:** L

**Criterios de aceptación**
- Dado un video con imágenes en sus comentarios, cuando la app termina la
  carga inicial, entonces veo una miniatura por cada imagen distinta.
- Dado que la búsqueda está en curso, cuando miro la pantalla, entonces veo
  que está trabajando y puedo cancelar.
- Dado que llegó la primera tanda de comentarios, cuando pasan 10 segundos o
  se completan 3 tandas, entonces aparece la cuadrícula con lo encontrado.
- Dado cualquier momento del uso, cuando la app trabaja con TikTok, entonces
  nunca me pide usuario ni contraseña de TikTok.
- Dado que la misma imagen aparece en dos comentarios, cuando veo la
  cuadrícula, entonces aparece una sola vez.

### US2.2 — Elegir los que quiero
**Como** usuario de stickers **quiero** marcar con un toque las imágenes que
quiero **para** guardar solo esas.
- **Requisitos:** FR3.2, FR3.3
- **Prioridad:** Must
- **Tamaño:** M

**Criterios de aceptación**
- Dada la cuadrícula, cuando toco una miniatura, entonces queda marcada y el
  contador sube; si la toco otra vez, se desmarca.
- Dado que no hay ninguna marcada, cuando miro "Guardar stickers", entonces
  está desactivado.

### US2.3 — Cargar más comentarios
**Como** usuario de stickers **quiero** pedir más comentarios **para** llegar
a un sticker que no estaba entre los primeros.
- **Requisitos:** FR2.4
- **Prioridad:** Should
- **Tamaño:** S

**Criterios de aceptación**
- Dado que ya marqué imágenes, cuando toco "Cargar más", entonces aparecen las
  nuevas y las marcadas siguen marcadas.
- Dado que no quedan más comentarios, cuando miro "Cargar más", entonces está
  desactivado.

### US2.4 — Video sin imágenes
**Como** usuario de stickers **quiero** que la app me diga cuando no encontró
imágenes **para** saber qué hacer en lugar de ver una pantalla vacía.
- **Requisitos:** FR3.4
- **Prioridad:** Must
- **Tamaño:** S

**Criterios de aceptación**
- Dado un video cuyos comentarios cargados no tienen imágenes, cuando termina
  la carga, entonces veo "Este video no tiene imágenes en los comentarios
  cargados" con las acciones "Cargar más" e "Importar imagen".

## Épica E3 — Convertir sin trabajo manual

### US3.1 — Sticker listo sin recortar
**Como** usuario de stickers **quiero** que la app ajuste sola cada imagen al
formato de WhatsApp **para** no recortar ni editar nada.
- **Requisitos:** FR4.1, FR4.2, FR4.3, FR4.6, FR4.7
- **Prioridad:** Must
- **Tamaño:** M

**Criterios de aceptación**
- Dada una imagen de cualquier tamaño o proporción, cuando la guardo,
  entonces el sticker mide 512×512, muestra la imagen completa y centrada con
  fondo transparente, y pesa 100 KB como máximo si es estático.
- Dado que toqué "Guardar stickers", cuando la app convierte, entonces no me
  muestra ninguna pantalla de edición ni me pide elegir nada.

### US3.2 — Conservar la animación
**Como** usuario de stickers **quiero** que un sticker animado siga animado en
WhatsApp **para** no perder lo que lo hace gracioso.
- **Requisitos:** FR4.4, FR4.5
- **Prioridad:** Should
- **Tamaño:** M

**Criterios de aceptación**
- Dada una imagen animada de 10 segundos o menos que cabe en 500 KB tras
  reducir calidad o cuadros, cuando la guardo, entonces el sticker es animado
  y no se acorta ni se acelera.
- Dada una imagen animada de más de 10 segundos, o que no cabe en 500 KB,
  cuando la guardo, entonces obtengo un sticker estático con su primer cuadro
  y el aviso "Se guardó sin animación".

### US3.3 — Saber qué pasó al guardar
**Como** usuario de stickers **quiero** un resumen de lo guardado **para**
saber si algo falló o quedó sin animación.
- **Requisitos:** FR4.8, FR5.9
- **Prioridad:** Must
- **Tamaño:** S

**Criterios de aceptación**
- Dado que elegí 3 imágenes y 1 no se pudo convertir, cuando termina,
  entonces se guardan 2 y veo "1 no se pudo convertir".
- Dado que mi selección tenía animados y estáticos, cuando termina, entonces
  el resumen dice cuántos fueron a cada paquete.

## Épica E4 — Tenerlos en WhatsApp

### US4.1 — Agregar el paquete a WhatsApp
**Como** usuario de stickers **quiero** que el paquete se agregue a WhatsApp
con solo confirmar **para** usar los stickers enseguida.
- **Requisitos:** FR5.1, FR5.3, FR5.4, FR5.5
- **Prioridad:** Must
- **Tamaño:** L

**Criterios de aceptación**
- Dado un paquete que llega a 3 o más stickers y aún no está en WhatsApp,
  cuando termina la conversión, entonces se abre la confirmación de WhatsApp
  sin que yo la pida y, al aceptar, el paquete aparece en WhatsApp.
- Dado un paquete con 1 o 2 stickers, cuando termina la conversión, entonces
  veo cuántos faltan para poder agregarlo y no se me ofrece agregarlo.
- Dado que guardé animados y estáticos, cuando miro mis paquetes, entonces
  están en paquetes distintos, con nombre e ícono puestos por la app.
- Dado que hay un paquete por agregar en cada serie, cuando termina la
  conversión, entonces confirmo uno y después el otro.

### US4.2 — Sumar stickers a un paquete que ya está en WhatsApp
**Como** usuario de stickers **quiero** que un sticker nuevo aparezca en
WhatsApp sin repetir el alta del paquete **para** guardar uno solo de un video
en segundos.
- **Requisitos:** FR5.6
- **Prioridad:** Must
- **Tamaño:** M

**Criterios de aceptación**
- Dado un paquete ya agregado a WhatsApp, cuando guardo un sticker más,
  entonces aparece en WhatsApp sin más acciones.
- Dado que WhatsApp exigiera volver a agregar el paquete, cuando guardo,
  entonces la app abre esa confirmación directamente.

### US4.3 — Paquete lleno
**Como** usuario de stickers **quiero** seguir guardando cuando un paquete
llega a 30 **para** no tener que gestionar paquetes a mano.
- **Requisitos:** FR5.2
- **Prioridad:** Must
- **Tamaño:** M

**Criterios de aceptación**
- Dado un paquete con 30 stickers, cuando guardo uno más de ese tipo,
  entonces existe un paquete nuevo con 3 (los 2 últimos del anterior y el
  nuevo), el anterior queda con 28 y no se pierde ni se duplica ninguno.

### US4.4 — Ver mis paquetes
**Como** usuario de stickers **quiero** ver mis paquetes y su estado **para**
saber qué tengo y agregar a WhatsApp el que esté pendiente.
- **Requisitos:** FR5.7, FR5.8
- **Prioridad:** Must
- **Tamaño:** S

**Criterios de aceptación**
- Dada la lista de paquetes, cuando la abro, entonces veo nombre, tipo,
  cantidad y estado de cada uno (faltan N / listo para agregar / agregado).
- Dado un paquete listo y no agregado, cuando toco "Agregar a WhatsApp",
  entonces se abre la confirmación de WhatsApp.
- Dado que reinicio el teléfono, cuando abro la app y WhatsApp, entonces mis
  paquetes siguen ahí.

### US4.5 — WhatsApp no está instalado
**Como** usuario de stickers **quiero** que la app me avise si no encuentra
WhatsApp **para** no perder lo que ya convertí.
- **Requisitos:** FR5.10
- **Prioridad:** Must
- **Tamaño:** S

**Criterios de aceptación**
- Dado un teléfono sin WhatsApp, cuando guardo stickers, entonces veo
  "WhatsApp no está instalado" y los stickers quedan en sus paquetes.

## Épica E5 — Cuando algo falla

### US5.1 — Entender por qué no funcionó
**Como** usuario de stickers **quiero** un mensaje claro cuando la búsqueda
falla **para** saber si reintentar o usar otra vía.
- **Requisitos:** FR6.1, FR6.2, FR6.3, FR6.4
- **Prioridad:** Must
- **Tamaño:** M

**Criterios de aceptación**
- Dado que no hay conexión, el video no existe, TikTok no muestra los
  comentarios, se agotó el tiempo o TikTok cambió, cuando falla la búsqueda,
  entonces veo un mensaje distinto para cada caso con "Reintentar" e
  "Importar imagen".
- Dado que no llega ningún comentario en 30 segundos, cuando se cumple ese
  tiempo, entonces la búsqueda se detiene y me avisa.
- Dado cualquier fallo de búsqueda, cuando ocurre, entonces la app no se
  cierra y mis paquetes siguen intactos.

### US5.2 — Importar una imagen de la galería
**Como** usuario de stickers **quiero** convertir una imagen que ya tengo en
el teléfono **para** no quedarme sin sticker cuando la búsqueda no funciona.
- **Requisitos:** FR7.1, FR7.2, FR7.3
- **Prioridad:** Should
- **Tamaño:** S

**Criterios de aceptación**
- Dada la pantalla de entrada, cuando toco "Importar imagen" y elijo una o
  varias, entonces se convierten y guardan igual que las extraídas.
- Dado un error de búsqueda, cuando toco "Importar imagen", entonces ocurre lo
  mismo.

## Épica E6 — Mantener la app

### US6.1 — Diagnosticar un fallo de extracción
**Como** mantenedor **quiero** ver el motivo técnico de un fallo de extracción
**para** arreglarlo rápido cuando TikTok cambie.
- **Requisitos:** NFR11, NFR12 (sin requisito funcional propio)
- **Prioridad:** Should
- **Tamaño:** S

**Criterios de aceptación**
- Dado un fallo de extracción, cuando consulto el registro de depuración del
  teléfono, entonces encuentro el tipo de fallo y el detalle técnico, sin
  datos personales.
- Dado que cambio la forma de extraer, cuando compilo, entonces no he tenido
  que modificar selección, conversión ni paquetes.

## Resumen

| Prioridad | Historias |
|---|---|
| Must | 13 |
| Should | 4 |

## Cobertura

| Requisito | Historias |
|---|---|
| FR1.1 | US1.1 |
| FR1.2 | US1.2 |
| FR1.3 | US1.1, US1.2 |
| FR1.4 | US1.3 |
| FR2.1, FR2.2, FR2.3, FR2.5, FR2.6, FR2.7 | US2.1 |
| FR2.4 | US2.3 |
| FR3.1 | US2.1 |
| FR3.2, FR3.3 | US2.2 |
| FR3.4 | US2.4 |
| FR4.1, FR4.2, FR4.3, FR4.6, FR4.7 | US3.1 |
| FR4.4, FR4.5 | US3.2 |
| FR4.8 | US3.3 |
| FR5.1, FR5.3, FR5.4, FR5.5 | US4.1 |
| FR5.2 | US4.3 |
| FR5.6 | US4.2 |
| FR5.7, FR5.8 | US4.4 |
| FR5.9 | US3.3 |
| FR5.10 | US4.5 |
| FR6.1 – FR6.4 | US5.1 |
| FR7.1 – FR7.3 | US5.2 |
