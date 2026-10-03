# Plan de entrega

Orden en que se construyen las unidades (bolts), qué debe demostrar cada una
al terminar y qué riesgos reduce. Cada bolt se construye en su rama y termina
con pruebas en verde, una comprobación en el teléfono y un pull request que la
persona revisa.

## Estrategia

**Riesgo primero.** U1 va primero por dependencia y porque prueba el riesgo
mayor (extracción sin sesión, RSK-01). Después U3, porque la conversión de
animados (RSK-03, RSK-04) es la siguiente incertidumbre técnica y U1 solo
convierte estáticos. Luego U2, que endurece la extracción frente a cambios y
errores (RSK-02). U4 completa paquetes y WhatsApp sobre la conversión ya
probada. U5 cierra con la interfaz completa y el criterio de salida.

## Bolts

| # | Unidad | Objetivo | Demostración al terminar | Riesgos que reduce |
|---|---|---|---|---|
| 1 | U1 — Esqueleto funcional | Proyecto creado y rebanada de punta a punta | En el teléfono: abrir la app, tocar "Probar", ver el sticker extraído de un video real en WhatsApp. Anotar si hizo falta sesión, si WhatsApp ve un sticker añadido después sin volver a agregar el paquete, y los tiempos medidos. `./gradlew test` y comprobaciones de calidad en verde | RSK-01, suposición de FR5.6, NFR2 inicial, puesta a punto del entorno |
| 2 | U3 — Conversión a sticker | Conversión completa, estática y animada, con caída a estático | En el teléfono: 3 imágenes de prueba (animada corta, animada larga, foto vertical) producen un animado válido, un estático con aviso y un estático centrado sin recorte; tiempos dentro de NFR2 | RSK-03, RSK-04 |
| 3 | U2 — Enlaces y extracción | Extracción robusta: cargar más, duplicados, tiempos, los cinco tipos de error | En el teléfono: 3 videos reales distintos muestran sus imágenes; en modo avión se obtiene "Sin conexión"; un enlace de perfil es rechazado. Pruebas del lector de respuestas con ejemplos reales guardados | RSK-02, RSK-07 |
| 4 | U4 — Paquetes y WhatsApp | Series, reparto al llenarse, almacenamiento atómico, acciones tras guardar | En el teléfono: animados y estáticos en paquetes separados; un sticker suelto aparece en WhatsApp en un paquete ya agregado; el reparto al pasar de 30 se comprueba con una prueba automática y se verifica en WhatsApp | RSK-05 |
| 5 | U5 — Interfaz completa | Flujo completo desde Compartir, con errores, resumen, paquetes e importación | El criterio de salida completo del alcance: 5 videos reales, menos de 1 minuto cada uno, sin recorte manual; importación desde la galería | Usabilidad, NFR1 |

## Hitos

- **H1 — Viabilidad confirmada** al terminar el bolt 1. Si la extracción no
  funciona, el plan se detiene y la persona decide la vía alternativa.
- **H2 — Núcleo técnico completo** al terminar los bolts 2 a 4.
- **H3 — Versión terminada** al cumplir el criterio de salida en el bolt 5.

Sin fechas: el proyecto no tiene fecha límite.

## Dependencias externas y bloqueos

Antes del bolt 1 la persona debe tener:

- **SDK de Android** instalado (Android Studio, o solo las herramientas de
  línea de comandos con `platform-tools`, la plataforma 36 y build-tools) y
  `ANDROID_HOME` definido. Sin él no se puede compilar ni ejecutar pruebas de
  `app`.
- **Teléfono Android** con depuración USB activada, conectado a la
  computadora, con TikTok y WhatsApp instalados.
- **Repositorio en GitHub** creado y configurado como remoto, y la
  herramienta `gh` con sesión iniciada (o crear los pull requests a mano).

Las pruebas del módulo `core` solo necesitan Java, que ya está instalado: esa
parte del bolt 1 puede avanzar antes de que el SDK esté listo.
