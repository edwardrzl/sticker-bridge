# Instrucciones de build y pruebas

Cómo compilar y probar Sticker Bridge desde un clon nuevo del repositorio.

## Requisitos

- JDK 17 o superior (el que trae Android Studio sirve).
- SDK de Android con la plataforma 37 y build-tools. Su ruta va en `local.properties`
  (`sdk.dir=...`), que no se sube al repositorio, o en la variable `ANDROID_HOME`.
- Para instalar y probar: un teléfono con Android 8.0 o superior, depuración USB
  activa, y TikTok y WhatsApp instalados.
- No hay variables de entorno ni secretos. La firma del build de publicación se lee de
  `keystore.properties` (ver `docs/release.md`).

En Windows usa `gradlew.bat` en vez de `./gradlew`. En una computadora con poca memoria
añade `--max-workers=1 -Pkotlin.compiler.execution.strategy=in-process` y cierra con
`./gradlew --stop` al terminar.

## Compilar

| Qué | Comando |
|---|---|
| Desde cero | `./gradlew clean` |
| APK de depuración | `./gradlew :app:assembleDebug` |
| APK de publicación | `./gradlew :app:assembleRelease` |
| Instalar en el teléfono | `./gradlew :app:installDebug` |

## Probar

| Capa | Comando | Necesita |
|---|---|---|
| Todas las pruebas unitarias | `./gradlew test` | Solo la computadora |
| Solo la lógica pura | `./gradlew :core:test` | Ni siquiera el SDK de Android |
| Solo la app | `./gradlew :app:testDebugUnitTest` | SDK de Android |
| Una clase | `./gradlew :core:test --tests "*ReplyQueueTest"` | |
| Capturas de pantalla | `./gradlew :app:validateDebugScreenshotTest` | SDK de Android |
| Formato | `./gradlew ktlintCheck` (corregir: `./gradlew ktlintFormat`) | |
| Análisis estático | `./gradlew detekt` | |
| Android Lint | `./gradlew :app:lintDebug` | SDK de Android |

Los informes quedan en `core/build/reports/` y `app/build/reports/`.

### Capturas de pantalla

Las pantallas se dibujan en la computadora y se comparan con las imágenes de
`app/src/screenshotTestDebug/reference/`. Si cambias el diseño a propósito, regenera las
imágenes con `./gradlew :app:updateDebugScreenshotTest`, míralas y súbelas con el cambio.

### En el teléfono

Lo que depende de TikTok y WhatsApp reales no se puede automatizar. Instala la app y
sigue `docs/manual-checklist.md`, anotando el resultado de cada paso. Para ver qué hace
la app mientras tanto: `adb logcat -s StickerBridge`.

## Antes de dar algo por terminado

`./gradlew test ktlintCheck detekt :app:lintDebug :app:validateDebugScreenshotTest` en
verde, y la lista manual ejecutada cuando el cambio toca la extracción, la conversión o
WhatsApp.
