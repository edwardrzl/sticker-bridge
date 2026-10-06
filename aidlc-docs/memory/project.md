# Memoria del proyecto

> Decisiones vigentes del proyecto, afirmadas por la persona en etapas de
> AI-DLC (Practices Discovery, Tech Stack Definition) o editadas a mano.
> Todas las sesiones de Claude en este proyecto leen este archivo (vía
> CLAUDE.md). Mantenlo breve: reglas y decisiones, no documentación.
>
> Las secciones se identifican por el texto en inglés entre paréntesis; no lo
> cambies.

## Stack tecnológico (Tech Stack)

| Área | Elección |
|---|---|
| Tipo de app | Android nativa, APK de depuración instalado directamente |
| Lenguaje / JDK | Kotlin 2.4.20 · Java 21 |
| Build | Gradle (Kotlin DSL, wrapper, catálogo de versiones) · Android Gradle Plugin 9.4.0 |
| SDK | compileSdk 37 · targetSdk 36 · minSdk 26 |
| Interfaz | Jetpack Compose + Material 3 (BOM 2026.09.00), ViewModel + `StateFlow`, corrutinas |
| Extracción | Android System WebView oculto con script inyectado, tras el contrato de `core/extraction` |
| Datos de TikTok / descarga | kotlinx.serialization · OkHttp 4.12 (la exige webp-android) |
| Conversión | `com.aureusapps.android:webp-android` (libwebp) tras una interfaz propia |
| Persistencia | Archivos internos + índice JSON con escritura atómica |
| WhatsApp | `ContentProvider` con el contrato oficial + intent `ENABLE_STICKER_PACK` |
| Dependencias | Inyección manual (contenedor de aplicación) |
| Pruebas / calidad | JUnit 5, kotlin.test, coroutines-test en la JVM · ktlint, detekt, Android Lint |
| Módulos | `core` (Kotlin puro, sin Android) y `app` (Android); `app` depende de `core`, nunca al revés |

Comandos (`gradlew.bat` en Windows):

- Pruebas: `./gradlew test`
- Lint y formato: `./gradlew ktlintCheck detekt :app:lintDebug` · corregir: `./gradlew ktlintFormat`
- Build: `./gradlew :app:assembleDebug`
- Instalar en el teléfono: `./gradlew :app:installDebug`
- Build de publicación: `./gradlew :app:assembleRelease` (ver `docs/release.md`)
- La computadora tiene poca memoria: añadir `--max-workers=1 -Pkotlin.compiler.execution.strategy=in-process` y cerrar con `./gradlew --stop` al terminar.

Detalle y ADR: `aidlc-docs/workflows/001-quiero-pasar-de-forma-sencilla-los-stick/inception/tech-stack/tech-stack.md`

## Forma de trabajo (Way of Working)

- **Ramas:** una rama por funcionalidad o unidad de trabajo (`feat/<nombre-corto>`, `fix/<nombre-corto>`), creada desde `main`.
- **Integración:** cada rama se une a `main` con un pull request en GitHub que describe qué cambia y cómo se probó. `main` siempre compila y pasa las pruebas.
- **Commits:** en inglés, Conventional Commits (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`), pequeños y con un solo propósito.
- **Quién hace commit:** Claude, al terminar cada paso aprobado del plan y solo cuando sus pruebas pasan.
- **Repositorio:** público en GitHub, pensado también como muestra de trabajo: historial ordenado y buena arquitectura. README que lo declara proyecto personal y educativo que usa una interfaz no oficial de TikTok. Incluye `aidlc-docs/`.
- **Terminado:** cumple el plan aprobado, pruebas unitarias en verde, formateador y analizador sin problemas, lista de comprobación manual ejecutada en el teléfono cuando aplica, pull request unido a `main`.

## Esqueleto funcional (Walking Skeleton)

- Sí, primero: enlace fijo de un video → extraer un sticker de sus comentarios en el teléfono → convertirlo al formato de WhatsApp → verlo en WhatsApp. Sin interfaz cuidada.
- Es la prueba de extracción que exige factibilidad. Si no logra extraer, se detiene la construcción y se decide con la persona la vía alternativa.

## Postura de pruebas (Testing Posture)

- **Metodología**: mixta
- **Orden**: en la lógica pura (conversión de imágenes, reglas del paquete, lectura de enlaces) se escribe primero la prueba y después el código; en pantallas y navegador interno se escribe primero el código y después la prueba.
- **Tipos:** pruebas unitarias de la lógica; la apariencia de las pantallas se comprueba con capturas generadas en la computadora (`./gradlew :app:validateDebugScreenshotTest`; se regeneran con `:app:updateDebugScreenshotTest`). Sin pruebas automáticas de interacción. (Capturas añadidas el 2026-10-06, confirmadas por la persona.)
- **Cobertura:** sin objetivo numérico; toda regla de negocio de la lógica pura tiene al menos una prueba.
- **Manual:** lista de comprobación en el teléfono real para lo que depende de TikTok y WhatsApp.

## Despliegue (Deployment)

- La app corre en el teléfono Android del usuario; no hay servidor.
- Se compila en la computadora del usuario y se instala por USB o copiando el archivo de instalación. Sin Google Play.
- Un solo entorno. Sin integración continua por ahora.

## Estilo de código (Code Style)

- Identificadores, comentarios, commits y registros en inglés; textos de la interfaz en español.
- Formateador y analizador estático estándar del lenguaje, con configuración versionada.
- Carpetas por funcionalidad (extracción, conversión, paquete, pantallas); dentro de cada una, lógica separada de interfaz y de sistemas externos.
- La lógica pura no depende de Android ni de TikTok; las funcionalidades se relacionan por interfaces explícitas.
- Errores explícitos y con tipo, sin fallos silenciosos; módulos pequeños, sin código muerto ni abstracciones especulativas.

## Reglas obligatorias (Mandated)

- SIEMPRE mantener la extracción de TikTok aislada detrás de una interfaz propia, reemplazable sin tocar el resto de la app.

## Reglas prohibidas (Forbidden)

- NUNCA pedir, usar ni guardar la sesión o las credenciales de TikTok.
- NUNCA enviar datos a servidores propios o de terceros; la app solo se comunica con TikTok y WhatsApp.
- NUNCA subir al repositorio la clave de firma de la app ni otros secretos.

## Glosario (Glossary)

<!-- Términos del dominio y su significado. -->
