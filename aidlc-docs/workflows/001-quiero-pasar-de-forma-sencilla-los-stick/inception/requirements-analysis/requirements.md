# Requisitos

Qué debe hacer la app (requisitos funcionales) y con qué calidad (no
funcionales), de forma verificable. Cada capacidad del documento de alcance
(`CAP-xx`) queda cubierta por al menos un requisito. Las decisiones de detalle
de esta etapa se tomaron por delegación de la persona (ver
`requirements-analysis-questions.md`). Incorpora los hallazgos de la revisión
independiente (`review.md`, R-01 a R-12).

## Resumen de la intención

Hoy, llevar a WhatsApp un sticker visto en un comentario de TikTok exige
captura de pantalla y recorte a mano, y pierde la animación. La persona quiere
una app Android propia, de uso personal, que a partir del video haga todo
sola: encontrar las imágenes de los comentarios, convertir las que elija y
dejarlas disponibles en WhatsApp, en menos de un minuto y a costo cero.

**Clasificación:** solicitud clara · producto nuevo · un solo sistema (app
Android) con dos integraciones externas · complejidad estándar, con un punto
de alto riesgo (extracción desde TikTok).

## Requisitos funcionales

### FR1 — Entrada del video

- **FR1.1** La app aparece como destino en el menú Compartir de Android para
  texto y enlaces, de modo que puede elegirse desde el botón Compartir de
  TikTok. — *Criterio de aceptación:* al compartir un video desde TikTok y
  elegir la app, esta se abre e inicia la extracción de ese video sin más
  pasos.
- **FR1.2** La app ofrece un campo para pegar el enlace y un botón para
  iniciar. — *Criterio de aceptación:* al pegar un enlace válido y confirmar,
  se inicia la extracción.
- **FR1.3** La app reconoce los enlaces de publicaciones de TikTok, incluso
  dentro de un texto más largo, en estas formas [assumption: la lista cubre
  las formas vigentes; se amplía si aparece otra]:
  `tiktok.com/@usuario/video/<id>`, `tiktok.com/@usuario/photo/<id>`,
  `m.tiktok.com/v/<id>`, `vm.tiktok.com/<código>`, `vt.tiktok.com/<código>` y
  `tiktok.com/t/<código>`. — *Criterio de aceptación:* hay una prueba unitaria
  por cada forma, sola y dentro de un texto.
- **FR1.4** Si el texto recibido no contiene un enlace de publicación de
  TikTok (otro sitio, texto sin enlace, o un enlace de TikTok que no es una
  publicación, como un perfil), la app lo indica y no inicia la extracción. —
  *Criterio de aceptación:* en los tres casos se muestra "No es un enlace de
  video de TikTok" y se permanece en la pantalla de entrada.

### FR2 — Extracción de imágenes de los comentarios

- **FR2.1** La app obtiene, en el propio teléfono y sin que el usuario inicie
  sesión en TikTok, las imágenes adjuntas a los comentarios de la publicación
  (stickers y fotos). — *Criterio de aceptación:* con un video real que tiene
  imágenes en sus comentarios, la app obtiene al menos una imagen sin pedir
  credenciales.
- **FR2.2** La carga inicial empieza a contar cuando llega la primera tanda de
  comentarios y se detiene al completar 3 tandas o al cumplirse 10 segundos
  desde esa primera tanda, lo que ocurra primero. — *Criterio de aceptación:*
  la cuadrícula aparece como máximo 10 segundos después de la llegada de la
  primera tanda, con lo encontrado hasta ese momento.
- **FR2.3** Mientras extrae, la app muestra que está trabajando y permite
  cancelar. — *Criterio de aceptación:* hay un indicador de progreso visible
  y, al cancelar, se vuelve a la pantalla de entrada.
- **FR2.4** La app ofrece "Cargar más" para traer una tanda adicional de
  comentarios y añade las imágenes nuevas a la cuadrícula sin perder la
  selección hecha. — *Criterio de aceptación:* tras pulsar "Cargar más"
  aparecen imágenes nuevas (si las hay) y las ya seleccionadas siguen
  seleccionadas; cuando no quedan más comentarios, el botón se desactiva.
- **FR2.5** Cada imagen encontrada se identifica por la dirección de su
  archivo original y no se muestra dos veces en la misma cuadrícula. —
  *Criterio de aceptación:* si el mismo archivo aparece en dos comentarios, la
  cuadrícula lo muestra una vez.
- **FR2.6** La extracción carga la publicación una sola vez por acción del
  usuario: no hay cargas en segundo plano ni reintentos automáticos. —
  *Criterio de aceptación:* tras un fallo, la app no vuelve a cargar hasta que
  el usuario pulsa "Reintentar"; sin acción del usuario no hay tráfico hacia
  TikTok.
- **FR2.7** El navegador interno que carga TikTok no es visible ni
  interactivo para el usuario; la app nunca le presenta la pantalla de inicio
  de sesión de TikTok. — *Criterio de aceptación:* en ningún punto del flujo
  el usuario puede escribir credenciales de TikTok dentro de la app.

### FR3 — Selección

- **FR3.1** Las imágenes encontradas se muestran en una cuadrícula de
  miniaturas estáticas de al menos 96 dp de lado, con el primer cuadro de cada
  imagen. — *Criterio de aceptación:* hay una miniatura por imagen encontrada.
- **FR3.2** Un toque marca o desmarca una imagen; la app muestra cuántas hay
  marcadas. — *Criterio de aceptación:* el contador coincide con las
  miniaturas marcadas.
- **FR3.3** Una acción "Guardar stickers" procesa todas las imágenes marcadas;
  está desactivada si no hay ninguna. — *Criterio de aceptación:* con cero
  marcadas el botón no responde; con una o más inicia la conversión.
- **FR3.4** Si la carga termina sin imágenes, la app lo dice expresamente en
  lugar de mostrar una cuadrícula vacía, y ofrece "Cargar más" (si quedan
  comentarios) e "Importar imagen" (FR7). — *Criterio de aceptación:* se
  muestra "Este video no tiene imágenes en los comentarios cargados" con ambas
  acciones.

### FR4 — Conversión a sticker de WhatsApp

- **FR4.1** Cada imagen elegida se convierte a WebP de 512×512 píxeles
  exactos. — *Criterio de aceptación:* todo archivo producido mide 512×512 y
  es WebP.
- **FR4.2** La imagen se escala, ampliando o reduciendo, hasta que su lado
  mayor mida 512 píxeles, conservando la proporción; se centra y el espacio
  sobrante queda transparente. No se recorta ni se deforma. — *Criterio de
  aceptación:* una imagen de 300×600 queda entera a 256×512 centrada; una de
  100×100 queda a 512×512.
- **FR4.3** Un sticker estático pesa como máximo 100 KB; la app reduce la
  calidad de compresión por pasos hasta cumplirlo. — *Criterio de
  aceptación:* ningún sticker estático producido supera 100 KB.
- **FR4.4** Una imagen de origen animada cuya duración total es de 10
  segundos o menos se convierte en sticker animado de como máximo 500 KB, con
  cuadros de al menos 8 milisegundos. Para cumplir el peso la app aplica, en
  este orden: (1) reducir la calidad de compresión por pasos hasta un mínimo
  de 30 sobre 100; (2) reducir los cuadros por segundo por pasos hasta un
  mínimo de 10. No acorta la duración ni acelera la animación. — *Criterio de
  aceptación:* una animación de 2 segundos produce un sticker animado dentro
  de los tres límites; todo sticker animado producido los cumple.
- **FR4.5** Si la imagen animada dura más de 10 segundos, o no cabe en 500 KB
  tras agotar los pasos de FR4.4, se entrega como sticker estático con su
  primer cuadro, y la app lo indica. — *Criterio de aceptación:* una animación
  de 12 segundos produce un sticker estático válido y el aviso "Se guardó sin
  animación".
- **FR4.6** La conversión no pide ninguna intervención manual (recortar,
  ajustar, elegir calidad). — *Criterio de aceptación:* entre "Guardar
  stickers" y el resultado no hay ninguna pantalla de edición.
- **FR4.7** A cada sticker se le asigna automáticamente el emoji por defecto
  😀, porque WhatsApp exige entre 1 y 3 por sticker. — *Criterio de
  aceptación:* todo sticker del paquete tiene exactamente ese emoji.
- **FR4.8** Si una imagen no puede descargarse o convertirse, se omite, las
  demás continúan y la app informa cuántas fallaron. — *Criterio de
  aceptación:* con 3 elegidas y 1 fallida, se guardan 2 y se muestra "1 no se
  pudo convertir".

### FR5 — Paquetes y entrega a WhatsApp

- **FR5.1** La app mantiene en el teléfono dos series de paquetes que crecen
  por separado: una de stickers animados y otra de estáticos, porque WhatsApp
  no admite mezclarlos. Cada sticker va al paquete abierto de la serie que
  corresponde a su resultado: un origen animado entregado como estático
  (FR4.5) va a la serie de estáticos. — *Criterio de aceptación:* ningún
  paquete contiene stickers de ambos tipos.
- **FR5.2** Un paquete admite como máximo 30 stickers. Cuando llega un sticker
  y el paquete abierto está lleno, se abre un paquete nuevo de la misma serie
  formado por los 2 últimos stickers del paquete lleno más el nuevo, de modo
  que el nuevo paquete nace con 3 y el anterior queda con 28. — *Criterio de
  aceptación:* al añadir el sticker 31 de una serie existen un primer paquete
  con 28 y un segundo con 3, y ningún sticker se pierde ni se duplica.
- **FR5.3** Cada paquete tiene, sin que el usuario lo proporcione: nombre
  "TikTok animados N" o "TikTok estáticos N" (N desde 1); un identificador
  único y estable de hasta 128 caracteres, solo con letras, números, "_", "-"
  y "."; un autor fijo ("TikTok Stickers"); y un ícono PNG de 96×96 píxeles de
  50 KB como máximo generado a partir de su primer sticker. — *Criterio de
  aceptación:* todo paquete creado cumple esas cuatro condiciones.
- **FR5.4** Un paquete con menos de 3 stickers se conserva pero no se ofrece a
  WhatsApp; la app muestra cuántos faltan. Por FR5.2 esto solo ocurre con el
  primer paquete de cada serie. — *Criterio de aceptación:* con 2 stickers se
  lee "Falta 1 sticker para poder agregarlo a WhatsApp" y no hay acción de
  agregar para ese paquete.
- **FR5.5** Al terminar la conversión, la app actúa por cada paquete afectado
  según su estado, sin que el usuario tenga que pedirlo:
  - *ya agregado a WhatsApp:* lo actualiza (FR5.6); no hay más acciones;
  - *con 3 o más stickers y aún no agregado:* abre directamente la
    confirmación de WhatsApp para agregarlo; si hay dos paquetes en este
    estado, una confirmación tras otra;
  - *con menos de 3:* muestra cuántos faltan (FR5.4).
  — *Criterio de aceptación:* en cada estado ocurre lo descrito; tras
  confirmar en WhatsApp, el paquete aparece en su selector de stickers.
- **FR5.6** Cuando se añaden stickers a un paquete que ya está en WhatsApp (o
  cambia su contenido por FR5.2), la app aumenta su número de versión y abre
  directamente la pantalla de WhatsApp para actualizarlo ("Updating this pack
  may add, remove or reorder stickers" → UPDATE). — *Criterio de aceptación:*
  tras añadir un sticker a un paquete ya agregado y tocar UPDATE, el sticker
  aparece en WhatsApp.
  > Actualizado en U3 (2026-10-04). El supuesto original —WhatsApp relee el
  > paquete solo con el cambio de versión— resultó falso en el teléfono:
  > WhatsApp consulta la ficha del paquete pero conserva su copia en caché y no
  > pide la lista de stickers ni los archivos. Solo los recarga cuando la app
  > vuelve a abrir su pantalla de alta, que para un paquete ya agregado ofrece
  > UPDATE.
- **FR5.7** Los paquetes y sus stickers persisten entre sesiones y reinicios
  del teléfono. — *Criterio de aceptación:* tras cerrar la app y reiniciar el
  teléfono, los paquetes siguen en la app y en WhatsApp.
- **FR5.8** La app muestra una lista de sus paquetes con nombre, tipo,
  cantidad de stickers y estado (faltan N / listo para agregar / agregado), y
  para los que están listos y no agregados ofrece "Agregar a WhatsApp". —
  *Criterio de aceptación:* el estado mostrado coincide con el real; la acción
  abre la confirmación de WhatsApp.
- **FR5.9** Tras guardar, la app resume el resultado: cuántos stickers se
  guardaron y en qué paquetes, cuáles quedaron sin animación y cuáles
  fallaron. — *Criterio de aceptación:* el resumen coincide con lo ocurrido,
  también cuando la selección se repartió entre las dos series.
- **FR5.10** Si WhatsApp no está instalado, la app lo indica; los stickers se
  guardan igualmente en sus paquetes. — *Criterio de aceptación:* sin WhatsApp
  se muestra "WhatsApp no está instalado" y no se pierde lo convertido.

### FR6 — Errores de extracción

- **FR6.1** La app distingue y comunica en lenguaje llano al menos estas
  situaciones: sin conexión a internet; la publicación no existe o no está
  disponible; TikTok no muestra los comentarios porque pide iniciar sesión,
  resolver una verificación o abrir su app; se agotó el tiempo de espera; la
  respuesta de TikTok no tiene la forma esperada (posible cambio de TikTok). —
  *Criterio de aceptación:* cada situación produce un mensaje distinto que
  dice qué pasó y qué puede hacer el usuario; se verifica con pruebas
  unitarias que simulan cada resultado de la extracción, y "sin conexión"
  además en el teléfono.
- **FR6.2** Todo mensaje de error de extracción ofrece "Reintentar" e
  "Importar imagen" (FR7). — *Criterio de aceptación:* ambas acciones están
  presentes y funcionan.
- **FR6.3** Si no llega la primera tanda de comentarios en 30 segundos desde
  que se inicia la extracción, esta se detiene y se informa. — *Criterio de
  aceptación:* nunca hay un indicador de carga indefinido.
- **FR6.4** Un fallo de extracción nunca cierra la app ni altera los paquetes
  guardados. — *Criterio de aceptación:* tras cualquier error de FR6.1 los
  paquetes existentes siguen intactos.

### FR7 — Importación manual (plan B)

- **FR7.1** La app permite elegir una o varias imágenes de la galería del
  teléfono. — *Criterio de aceptación:* se abre el selector de imágenes del
  sistema y las elegidas vuelven a la app.
- **FR7.2** Las imágenes importadas pasan por la misma conversión (FR4) y se
  guardan en los mismos paquetes, con las mismas reglas (FR5), que las
  extraídas. — *Criterio de aceptación:* una imagen importada termina en el
  paquete de la serie que corresponde a su resultado, con el mismo formato que
  una extraída.
- **FR7.3** La importación está disponible desde la pantalla de entrada, no
  solo tras un error. — *Criterio de aceptación:* hay una acción "Importar
  imagen" visible sin haber intentado una extracción.

### Cobertura de capacidades

| Capacidad | Requisitos |
|---|---|
| CAP-01 Extraer en el teléfono | FR2.1, FR2.2, FR2.3, FR2.5, FR2.6, FR2.7 |
| CAP-02 Recibir desde Compartir | FR1.1, FR1.3, FR1.4 |
| CAP-03 Recibir pegando el enlace | FR1.2, FR1.3, FR1.4 |
| CAP-04 Cuadrícula y selección | FR3.1, FR3.2, FR3.3 |
| CAP-05 Conversión automática | FR4.1, FR4.2, FR4.3, FR4.6, FR4.7, FR4.8 |
| CAP-06 Paquetes que crecen y entrega de un toque | FR5.1 – FR5.10 |
| CAP-07 Mensajes claros de error | FR3.4, FR6.1 – FR6.4 |
| CAP-08 Conservar la animación | FR4.4, FR4.5 |
| CAP-09 Cargar más | FR2.4 |
| CAP-10 Plan B manual | FR7.1 – FR7.3 |

FR7.1 (varias imágenes a la vez) y FR7.3 (importar desde la pantalla de
entrada) van un poco más allá del texto de CAP-10, sin costo relevante.

## Requisitos no funcionales

| ID | Categoría | Requisito | Meta medible | Cómo se verifica |
|---|---|---|---|---|
| NFR1 | Rendimiento | Tiempo total del flujo principal, con el paquete ya agregado a WhatsApp, eligiendo hasta 3 stickers, con wifi o 4G | Menos de 60 s del enlace recibido al sticker usable en WhatsApp. Presupuesto: primera tanda ≤ 15 s; carga inicial ≤ 10 s; elección del usuario ≈ 10 s; conversión ≤ 20 s en total; actualización en WhatsApp ≤ 5 s | Cronometrado en el teléfono con 5 videos reales |
| NFR2 | Rendimiento | Conversión por sticker | Estático: menos de 2 s. Animado: menos de 6 s | Medición en el teléfono; tiempos en el registro de depuración |
| NFR3 | Rendimiento | Capacidad de respuesta | Durante extracción y conversión la interfaz responde a un toque en menos de 1 s y "Cancelar" surte efecto en menos de 2 s | Lista de comprobación manual |
| NFR4 | Privacidad | Sin sesión de TikTok | La app no pide ni acepta credenciales de TikTok (FR2.7). Las cookies y datos del navegador interno, incluidas las anónimas que TikTok crea, se borran al terminar cada extracción | Revisión de código; lista de comprobación |
| NFR5 | Privacidad | Sin envío de datos por la app | El código propio solo hace peticiones para descargar las imágenes encontradas, desde dominios de TikTok; no incluye librerías de analítica, publicidad ni reporte de fallos | Revisión de dependencias y de código |
| NFR6 | Privacidad | Tráfico del navegador interno | El navegador interno solo permite peticiones a una lista de dominios de TikTok y de su red de archivos; el resto se bloquea [assumption: la página funciona con ese bloqueo; si no, la lista se amplía a lo imprescindible y se documenta] | Prueba unitaria de la regla de dominios; comprobación en el esqueleto |
| NFR7 | Seguridad | Permisos mínimos | Solo acceso a internet; la galería se usa mediante el selector del sistema, sin permiso de almacenamiento | Revisión del manifiesto de la app |
| NFR8 | Seguridad | Exposición de los stickers | Los paquetes solo son legibles por WhatsApp, mediante el permiso de lectura que WhatsApp define | Revisión del manifiesto |
| NFR9 | Fiabilidad | Integridad de los paquetes | Un fallo o cierre a mitad de operación no deja un paquete inválido: añadir un sticker o repartir un paquete lleno (FR5.2) se completa entero o no se aplica | Prueba unitaria de las operaciones de guardado |
| NFR10 | Fiabilidad | Validez ante WhatsApp | Todo paquete ofrecido cumple los límites oficiales: 3–30 stickers, tipo único, 512×512, pesos, duración, ícono, emoji, nombre, identificador y autor | Pruebas unitarias de validación antes de ofrecer el paquete |
| NFR11 | Mantenibilidad | Extracción reemplazable | Cambiar la forma de extraer no obliga a modificar selección, conversión ni paquetes | Revisión de dependencias entre módulos |
| NFR12 | Mantenibilidad | Diagnóstico | Cuando la extracción falla, el motivo técnico queda en el registro de depuración del dispositivo, sin datos personales | Revisión de código |
| NFR13 | Compatibilidad | Dispositivo | Funciona en el teléfono Android del usuario; la versión mínima de Android se fija en la definición del stack | Instalación y prueba en ese teléfono |
| NFR14 | Usabilidad | Idioma y brevedad | Interfaz en español. Tras recibir el video: elegir, guardar, y una confirmación en WhatsApp por cada paquete afectado (agregar o UPDATE): 3 acciones con una serie, 4 como máximo con las dos. Actualizado en U3: WhatsApp exige confirmar también las actualizaciones (FR5.6) | Lista de comprobación manual |
| NFR15 | Accesibilidad | Mínimos | Zonas táctiles de al menos 48 dp; botones con etiqueta de texto; respeta el tema claro u oscuro del sistema | Lista de comprobación manual |
| NFR16 | Costo | Operación | Cero: sin servidor, sin servicios de pago, sin cuota de tienda | Revisión de dependencias |
| NFR17 | Almacenamiento | Espacio | Como máximo 500 KB por sticker guardado; los archivos temporales de descarga se borran al terminar cada conversión | Prueba unitaria y revisión |

## Restricciones

- App Android instalada directamente en el teléfono del usuario; sin Google
  Play.
- Extracción en el dispositivo mediante un navegador interno que carga la web
  de TikTok; interfaz no oficial, puede cambiar sin aviso.
- Entrega mediante el mecanismo oficial de paquetes de stickers de WhatsApp
  para apps Android, con sus límites: paquete de 3 a 30 stickers, todos
  estáticos o todos animados; sticker WebP de 512×512; estático ≤ 100 KB;
  animado ≤ 500 KB, ≤ 10 s en total y ≥ 8 ms por cuadro; ícono PNG de 96×96
  ≤ 50 KB; de 1 a 3 emojis por sticker; nombre, identificador y autor de hasta
  128 caracteres.
- Reglas del proyecto (`project.md`): nunca la sesión de TikTok; nunca datos a
  servidores; nunca secretos en el repositorio; extracción siempre aislada.
- Costo cero, una persona, sin fecha límite.

## Supuestos

- [assumption] TikTok muestra los comentarios con sus imágenes en la versión
  web sin iniciar sesión, y un navegador interno puede leer sus direcciones.
  Lo confirma el esqueleto funcional (RSK-01).
- [assumption] WhatsApp relee un paquete ya agregado cuando cambia su número
  de versión (FR5.6), incluido el caso en que un paquete pierde 2 stickers por
  FR5.2.
- [assumption] No se puede distinguir de forma fiable un sticker de una foto;
  se muestran todas las imágenes.
- [assumption] Las publicaciones de fotos (`/photo/<id>`) exponen sus
  comentarios igual que los videos.
- [assumption] Los tiempos de NFR1 y NFR2 son alcanzables en el teléfono del
  usuario; se miden en el esqueleto funcional y se ajustan si no.
- [assumption] El borde blanco de 8 píxeles que WhatsApp recomienda es una
  pauta de diseño y no una validación; no se aplica.
- Decisiones delegadas de esta etapa: esperar a 3 stickers en el primer
  paquete de cada serie (FR5.4), dos series de paquetes (FR5.1), reparto al
  llenarse un paquete (FR5.2), carga inicial de 3 tandas o 10 s (FR2.2),
  ajuste sin recorte (FR4.2), nombres, autor y emoji automáticos (FR5.3,
  FR4.7), bloqueo de dominios ajenos en el navegador interno (NFR6), solo
  WhatsApp normal, duplicados permitidos.

## Fuera del alcance

- Quitar stickers guardados, evitar duplicados entre usos y vista previa
  animada en la cuadrícula (Futuro).
- Edición o recorte manual; elección de nombre de paquete o emoji.
- WhatsApp Business.
- Stickers de relleno para completar paquetes de menos de 3.
- Favoritos de la cuenta de TikTok; otros destinos; cuentas o nube; iPhone;
  web; Google Play.
- Servidor propio, proxies o scrapers de pago. Solo se reconsideran si el
  esqueleto funcional demuestra que la extracción en el teléfono no funciona,
  y lo decide la persona (condición 2 de factibilidad).

## Preguntas abiertas

- **Los 2 primeros stickers de cada serie no aparecen en WhatsApp hasta que
  llega el tercero.** Ocurre una sola vez por serie (animados y estáticos),
  porque al llenarse un paquete el siguiente nace con 3 (FR5.2). El criterio
  de salida 4 y CAP-06 del documento de alcance se actualizaron en esta etapa
  para reflejarlo. Es una decisión delegada: si a la persona no le convence,
  la alternativa es completar con stickers de relleno temporales (Q1, opción
  B).
- **A resolver en el esqueleto funcional:** si la extracción funciona sin
  sesión (RSK-01) y con el bloqueo de dominios (NFR6), si FR5.6 se cumple sin
  volver a agregar el paquete, y si los tiempos de NFR1 y NFR2 son realistas.
- **A resolver en la definición del stack:** versión mínima de Android y
  librería para producir WebP animado dentro de los límites.

## Fuentes

- https://github.com/WhatsApp/stickers/blob/main/Android/README.md (límites
  oficiales, consultados el 2026-10-02)
