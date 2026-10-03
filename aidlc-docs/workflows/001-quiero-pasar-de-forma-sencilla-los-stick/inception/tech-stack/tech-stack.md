# Stack tecnológico

Tecnología con la que se construye la app y por qué, derivada de los
requisitos. Las elecciones se hicieron con la opción recomendada por
delegación de la persona (ver `tech-stack-questions.md`). Las decisiones
difíciles de revertir tienen su ADR en `adr/`.

## Resumen

| Área | Elección | Versión | Motivo (req./restricción) |
|---|---|---|---|
| Tipo de aplicación | App Android nativa, instalada directamente (APK de depuración) | — | Factibilidad: única vía que cumple animación, paquete de un toque y costo cero (NFR16) |
| Lenguaje | Kotlin | 2.4.20 | Las tres integraciones son nativas (navegador interno, proveedor de contenido, libwebp); ADR-001 |
| JDK | Java (ya instalado) | 21 | Requisito de compilación de Android Gradle Plugin (mínimo 17) |
| Build | Gradle con Kotlin DSL, wrapper y catálogo de versiones | Gradle 9.6 `[verificar versión]` · Android Gradle Plugin 9.4.0 | Estándar de Android; reproducible sin instalar Gradle |
| SDK de Android | compileSdk y targetSdk 36 · minSdk 26 (Android 8.0) | 36 `[verificar versión]` | NFR13; sin código de compatibilidad relevante |
| Interfaz | Jetpack Compose + Material 3 | Compose BOM 2026.09.00 | Pocas pantallas, NFR14 y NFR15 (tema del sistema, zonas táctiles) |
| Arquitectura de presentación | ViewModel + estado inmutable con `StateFlow` | AndroidX Lifecycle `[verificar versión]` | NFR3: trabajo fuera del hilo de interfaz |
| Concurrencia | Corrutinas de Kotlin | kotlinx-coroutines 1.x `[verificar versión]` | Cancelación (FR2.3), tiempos límite (FR6.3) |
| Extracción | Android System WebView, oculto, con script inyectado que captura las respuestas de la lista de comentarios, y filtro de dominios en `shouldInterceptRequest` | del sistema | FR2.1, FR2.7, NFR6; TikTok exige un navegador real; ADR-002 |
| Lectura de datos de TikTok | kotlinx.serialization (JSON, tolerante a campos desconocidos) | 1.x `[verificar versión]` | FR6.1: detectar respuestas con forma inesperada |
| Descarga de imágenes | OkHttp | 5.x `[verificar versión]` | FR4.8: tiempos límite y errores explícitos |
| Conversión de imágenes | `com.aureusapps.android:webp-android` (libwebp por JNI): lee y escribe WebP estático y animado | 1.1.2 `[verificar versión]` | FR4.1–FR4.5; Android no trae codificador de WebP animado; ADR-003 |
| Miniaturas | Coil para Compose | 3.x `[verificar versión]` | FR3.1 |
| Persistencia | Archivos en almacenamiento interno + índice JSON con escritura atómica | — | FR5.7, NFR9; decenas de registros; ADR-004 |
| Entrega a WhatsApp | `ContentProvider` propio con el contrato oficial de paquetes de stickers + intent `com.whatsapp.intent.action.ENABLE_STICKER_PACK` | — | FR5.5, FR5.6, NFR8 |
| Inyección de dependencias | Manual: un contenedor de aplicación y constructores | — | Proyecto pequeño; evita un marco y su generación de código |
| Pruebas | JUnit 5 + kotlin.test + kotlinx-coroutines-test, en la JVM | JUnit 5.x `[verificar versión]` | Postura de pruebas: unitarias de la lógica, sin emulador |
| Calidad | ktlint (formato), detekt (análisis), Android Lint | `[verificar versión]` | Estilo de código del proyecto |
| Registro | `android.util.Log` detrás de una interfaz propia | — | NFR12; sin librerías de reporte (NFR5) |
| Autenticación | Ninguna | — | Un solo usuario; nunca la sesión de TikTok |
| Servidor, base de datos remota, mensajería | Ninguno | — | NFR16 y regla "nunca datos a servidores" |

No aplican: framework backend, frontend web, colas, observabilidad remota.

## Estructura del proyecto

Un repositorio, un proyecto Gradle con dos módulos. Dentro de cada módulo, un
paquete por funcionalidad.

```
stickers-tiktok-whatsapp/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml        catálogo de versiones
├── core/                            Kotlin puro (JVM), sin Android
│   └── src/{main,test}/kotlin/<paquete>/
│       ├── link/                    reconocer enlaces de TikTok
│       ├── extraction/              contrato del extractor, lectura de respuestas, tipos de error
│       ├── conversion/              cálculo de encaje a 512, pasos de calidad, límites
│       └── pack/                    reglas de paquetes, validación, reparto al llenarse
├── app/                             Android
│   └── src/main/kotlin/<paquete>/
│       ├── extraction/              extractor con WebView (implementa el contrato de core)
│       ├── conversion/              codificación WebP y descarga
│       ├── pack/                    almacenamiento, ContentProvider, intent de WhatsApp
│       ├── ui/                      pantallas Compose y ViewModels
│       └── di/                      contenedor de dependencias
├── docs/                            lista de comprobación manual
└── aidlc-docs/                      documentos de proceso
```

- `app` depende de `core`; `core` no depende de nada de Android. El compilador
  hace cumplir la regla "la lógica pura no depende de Android ni de TikTok".
- Nombre de trabajo de la app: "Sticker Bridge"; identificador
  `com.edrl.stickerbridge` [assumption: cambiable antes de la primera
  compilación; se evita usar marcas ajenas en el identificador].

## Comandos estándar

En Windows, `gradlew.bat` en lugar de `./gradlew`.

| Acción | Comando |
|---|---|
| Instalar | `./gradlew --version` (el wrapper descarga Gradle; las dependencias se resuelven al compilar) |
| Ejecutar en local | `./gradlew :app:installDebug` con el teléfono conectado por USB y depuración activada |
| Pruebas | `./gradlew test` |
| Lint / formato | `./gradlew ktlintCheck detekt :app:lintDebug` · corregir formato: `./gradlew ktlintFormat` |
| Build | `./gradlew :app:assembleDebug` (genera `app/build/outputs/apk/debug/app-debug.apk`) |

## Requisitos del entorno

Faltan en la computadora de la persona y son necesarios antes de la primera
compilación:

- **SDK de Android** (herramientas de línea de comandos, `platform-tools` con
  `adb`, plataforma 36 y build-tools). Basta instalar Android Studio, o solo
  las herramientas de línea de comandos, y definir `ANDROID_HOME`.
- **Teléfono con depuración USB activada** para instalar y para la lista de
  comprobación manual.
- **Repositorio en GitHub y la herramienta `gh`** (o un remoto configurado)
  para los pull requests que pide la forma de trabajo.

Ya presentes: Java 21, Node 20, git.

## Alternativas descartadas

| Alternativa | Por qué no |
|---|---|
| React Native / Capacitor | Navegador interno con interceptación, proveedor de contenido y libwebp habría que escribirlos igualmente en nativo, más el puente |
| Flutter | Mismo problema, y añade un SDK más que instalar |
| Vistas XML | Más código para las mismas pantallas |
| Room / SQLite | Decenas de registros con una sola relación; no justifica esquema ni migraciones |
| Reimplementar las firmas de TikTok y llamar a su API directamente | Frágil; TikTok descarta el tráfico que no viene de un navegador aunque esté firmado |
| Compilar libwebp propio con el NDK | Más control, pero añade NDK y CMake al entorno; se reconsidera si la librería elegida no cumple (ver riesgos) |
| Hilt / Koin | Un contenedor manual basta para este tamaño |
| Pruebas de interfaz instrumentadas | Fuera de la postura de pruebas acordada |

## Riesgos y decisiones irreversibles

- **Lenguaje y plataforma (ADR-001):** difícil de revertir. Coherente con
  todas las restricciones.
- **Extracción con WebView (ADR-002):** es el riesgo principal (RSK-01). Queda
  detrás del contrato de `core/extraction`, así que es la pieza más fácil de
  sustituir.
- **Librería de WebP (ADR-003):** dependencia de un tercero con un solo
  mantenedor. Queda detrás de una interfaz de conversión; el plan B es
  compilar libwebp con el NDK. Su rendimiento se mide en el esqueleto
  funcional (NFR2).
- **Formato del índice de paquetes (ADR-004):** lleva número de versión de
  esquema para poder migrarlo.
- **Identificador de la app:** cambiarlo después de agregar paquetes a
  WhatsApp los deja huérfanos. Debe fijarse antes del primer uso real.

## Supuestos y preguntas abiertas

- [assumption] La persona no tiene experiencia previa en Android (Q1 sin
  responder).
- [assumption] Las versiones marcadas `[verificar versión]` se confirman al
  crear el proyecto, tomando la última estable compatible con Android Gradle
  Plugin 9.4.0 y Kotlin 2.4.20.
- [assumption] `webp-android` permite controlar calidad y duración por cuadro
  lo suficiente para FR4.4.
- [assumption] El teléfono de la persona tiene Android 8.0 o superior.
- **Abierta:** nombre definitivo e identificador de la app.

## Fuentes

- https://developer.android.com/build/releases/agp-9-4-0-release-notes
- https://blog.jetbrains.com/kotlin/2026/09/kotlin-2-4-20-released/
- https://developer.android.com/develop/ui/compose/bom
- https://github.com/UdaraWanasinghe/webp-android
- https://github.com/WhatsApp/stickers/blob/main/Android/README.md
