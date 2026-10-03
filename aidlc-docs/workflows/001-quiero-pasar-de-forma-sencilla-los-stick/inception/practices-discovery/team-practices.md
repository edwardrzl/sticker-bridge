# Prácticas del equipo

Cómo se construye este proyecto: forma de trabajo con git, orden de
construcción, pruebas, instalación, estilo y reglas fijas. Una vez aprobado,
este contenido pasa a `aidlc-docs/memory/project.md` y rige todas las sesiones
futuras. El equipo es una sola persona con Claude como asistente; el
repositorio será público en GitHub y debe servir como muestra de trabajo.

> Las secciones "Estilo de código" y "Reglas" se fijaron con las opciones
> recomendadas por delegación de la persona ("avanza con las opciones
> recomendadas", "lo que recomiendes"). Están pendientes de su revisión en la
> aprobación de esta etapa.

## Forma de trabajo

- **Ramas:** una rama por funcionalidad o unidad de trabajo
  (`feat/<nombre-corto>`, `fix/<nombre-corto>`), creada desde `main`.
- **Integración:** cada rama se une a `main` mediante un pull request en GitHub
  con descripción de qué cambia y cómo se probó. `main` siempre compila y pasa
  las pruebas.
- **Revisión:** no hay segundo revisor; la persona aprueba el plan antes del
  código y revisa el pull request antes de unirlo.
- **Mensajes de commit:** en inglés, formato Conventional Commits
  (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`), commits pequeños
  y con un solo propósito.
- **Quién hace commit:** Claude, al terminar cada paso aprobado del plan y solo
  cuando sus pruebas pasan.
- **Repositorio:** público en GitHub, con un README que lo declara proyecto
  personal y educativo que usa una interfaz no oficial de TikTok. Incluye los
  documentos de proceso de `aidlc-docs/`.
- **Definición de terminado:** el código cumple el plan aprobado, las pruebas
  unitarias pasan, el formateador y el analizador no reportan problemas, la
  lista de comprobación manual aplicable se ejecutó en el teléfono, y el pull
  request está unido a `main`.

## Esqueleto funcional

- **Sí, primero.** Antes de cualquier funcionalidad completa se construye una
  rebanada mínima de punta a punta: un enlace fijo de un video → extraer un
  sticker de sus comentarios en el teléfono → convertirlo al formato de
  WhatsApp → verlo en WhatsApp. Sin interfaz cuidada.
- Sirve de prueba de extracción exigida por factibilidad (RSK-01) y comprueba
  además la entrega a WhatsApp.
- Si el esqueleto no logra extraer, se detiene la construcción y se decide con
  la persona la vía alternativa (computadora de casa o scraper de pago).

## Postura de pruebas

- **Metodología**: mixta
- **Orden**: en la lógica pura (conversión de imágenes, reglas del paquete,
  lectura de enlaces) se escribe primero la prueba y después el código; en
  pantallas y navegador interno se escribe primero el código y después la
  prueba.
- **Tipos:** pruebas unitarias de la lógica. Sin pruebas automáticas de
  interfaz.
- **Cobertura:** sin objetivo numérico; toda regla de negocio de la lógica pura
  tiene al menos una prueba.
- **Comprobación manual:** una lista de comprobación en el teléfono real cubre
  lo que depende de TikTok y de WhatsApp (extracción, agregar el paquete,
  animación), porque no se puede automatizar de forma estable.

## Despliegue

- **Dónde corre:** en el teléfono Android del usuario. No hay servidor.
- **Cómo se instala:** la app se compila en la computadora del usuario y se
  instala por cable USB o copiando el archivo de instalación.
- **Entornos:** uno solo (el teléfono del usuario). Sin Google Play.
- **Cadencia:** cuando una funcionalidad está terminada y unida a `main`.
- **Integración continua:** ninguna por ahora [assumption: puede añadirse más
  adelante para ejecutar las pruebas en cada pull request, sin costo].

## Estilo de código

- **Idioma:** identificadores, comentarios, mensajes de commit y de registro en
  inglés; textos de la interfaz en español.
- **Formato y análisis:** el formateador y el analizador estático estándar del
  lenguaje que se elija en la definición del stack, con su configuración por
  defecto y versionada en el repositorio.
- **Organización:** carpetas por funcionalidad (extracción, conversión,
  paquete, pantallas); dentro de cada una, la lógica separada de la interfaz y
  del acceso a sistemas externos.
- **Dependencias entre funcionalidades:** solo a través de interfaces
  explícitas; la lógica pura no depende de la plataforma Android ni de TikTok.
- **Errores:** explícitos y con tipo; nada de fallos silenciosos. Todo error de
  extracción llega al usuario como un mensaje comprensible.
- **Tamaño:** módulos pequeños y cohesivos; sin código muerto ni abstracciones
  especulativas.

## Reglas obligatorias

- SIEMPRE mantener la extracción de TikTok aislada detrás de una interfaz propia, reemplazable sin tocar el resto de la app.

## Reglas prohibidas

- NUNCA pedir, usar ni guardar la sesión o las credenciales de TikTok.
- NUNCA enviar datos a servidores propios o de terceros; la app solo se comunica con TikTok y WhatsApp.
- NUNCA subir al repositorio la clave de firma de la app ni otros secretos.

## Supuestos y preguntas abiertas

- [assumption] Todavía no existe el repositorio en GitHub ni un remoto
  configurado; hay que crearlo antes del primer pull request.
- [assumption] La integración continua queda fuera por ahora.
- El repositorio no tiene ningún commit: el primero incluirá la estructura
  actual y los documentos de proceso.
- Publicar el código hace visible el uso de una interfaz no oficial de TikTok;
  la persona aceptó el riesgo (bajo) de que GitHub retire el repositorio a
  petición de la plataforma.
