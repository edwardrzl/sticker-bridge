# Preguntas — Tech Stack Definition

> Responde cada pregunta escribiendo la letra (y, si quieres, un comentario)
> después de `[Answer]:`. Para varias opciones: `[Answer]: A, C`.
> Si ninguna opción encaja, usa `X` y explica.

> **Respuestas delegadas.** La persona pidió avanzar sin consultarla. Todas las
> respuestas son la opción recomendada, elegida por Claude. Q1 pregunta un
> hecho que solo la persona conoce: queda como supuesto, no como decisión.

## Q1 — Con qué lenguajes te sientes cómodo
La experiencia pesa mucho al elegir: vas a mantener este código y a mostrarlo.
¿Qué has usado para desarrollo Android o móvil?

A. Kotlin o Java en Android
B. JavaScript/TypeScript (React Native, Capacitor u otro híbrido)
C. Dart con Flutter
D. Nada de móvil todavía
X. Otra (especifica)

[Answer]: Sin respuesta de la persona. Supuesto de trabajo: D — sin experiencia móvil previa. Evidencia: en la computadora hay Java 21, Node 20 y Python, pero no el SDK de Android ni Flutter. No condiciona la elección de Q2 porque las integraciones exigen código nativo en cualquier caso.

## Q2 — Tipo de app y lenguaje
La app necesita tres piezas nativas de Android: un navegador interno con interceptación de peticiones, un proveedor de contenido que WhatsApp lee, y codificación de WebP animado con una librería en C.
¿Con qué se construye?

A. Android nativo con Kotlin (recomendada — las tres piezas son nativas; un marco híbrido obligaría a escribir igualmente ese código nativo más el puente)
B. React Native o Capacitor con módulos nativos propios
C. Flutter con canales de plataforma
X. Otra (especifica)

[Answer]: A — Android nativo con Kotlin (delegada)

## Q3 — Interfaz
Son pocas pantallas: entrada, cuadrícula, resultado y lista de paquetes.
¿Con qué se hace la interfaz?

A. Jetpack Compose con Material 3 (recomendada — es el estándar actual de Android, menos código para pantallas simples, bueno como muestra de trabajo)
B. Vistas XML clásicas
X. Otra (especifica)

[Answer]: A — Jetpack Compose con Material 3 (delegada)

## Q4 — Dónde se guardan los paquetes
Hay que guardar los archivos de los stickers y unos pocos datos por paquete (nombre, tipo, lista de stickers, versión, si ya está en WhatsApp).
¿Cómo se persiste?

A. Archivos en el almacenamiento interno de la app más un archivo de índice en JSON escrito de forma atómica (recomendada — son decenas de registros; una base de datos sería una abstracción sin necesidad)
B. Base de datos SQLite con Room
C. Jetpack DataStore
X. Otra (especifica)

[Answer]: A — Archivos más índice JSON (delegada)

## Q5 — Versión mínima de Android
Una versión mínima baja exige más código de compatibilidad; una alta puede dejar fuera tu teléfono.
¿Qué versión mínima se admite?

A. Android 8.0 (API 26) (recomendada — cubre prácticamente cualquier teléfono en uso y no obliga a código de compatibilidad relevante)
B. Android 11 (API 30)
C. Solo la versión de tu teléfono
X. Otra (especifica)

[Answer]: A — Android 8.0, API 26 (delegada; se confirma al instalar en el teléfono)

## Q6 — Cómo se organiza el proyecto
Las reglas del proyecto piden que la lógica pura no dependa de Android ni de TikTok y que se pruebe primero.
¿Uno o varios módulos?

A. Dos módulos: `core` en Kotlin puro (enlaces, reglas de paquetes, plan de conversión, contratos) y `app` con todo lo de Android (recomendada — el compilador impide que la lógica dependa de Android, y sus pruebas corren en segundos sin emulador)
B. Un solo módulo `app` con separación por carpetas
C. Un módulo por funcionalidad
X. Otra (especifica)

[Answer]: A — Dos módulos, `core` y `app` (delegada)
