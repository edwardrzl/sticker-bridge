# Avance del plan — U1 — Esqueleto funcional

Copia de los pasos del plan aprobado con su estado real (el plan aprobado no se
modifica). Rama: `feat/u1-walking-skeleton`.

- [x] **Paso 1 — Proyecto y herramientas.** Gradle 9.8.0 (wrapper), AGP 9.4.1, Kotlin
  2.4.20, catálogo de versiones, ktlint 14.2.0, detekt 1.23.8. Commit `8dd8d48`.
  `app` configura pero no compila: falta el SDK de Android.
- [x] **Paso 2 — Tipos y puertos de `core`.** Commit `b9a66a8`.
- [x] **Paso 3 — Lectura de respuestas y dominios.** 12 pruebas. Commit `53e104a`.
- [x] **Paso 4 — Encaje y calidad estática.** 11 pruebas. Commit `d4316fa`.
- [x] **Paso 5 — Reglas mínimas de paquetes.** 18 pruebas. Commit `49b3bc8`.
- [x] **Paso 6 — Adaptadores de `app`.** Commits `30936cb` y `298d622` (correcciones de
  compilación y fallo del renderizador del navegador). `FilePackRepositoryTest`: 5 pruebas
  en verde.
- [x] **Paso 7 — Pantalla de prueba y manifiesto.** Commit `daa2c3c`. APK de depuración
  generado (`app/build/outputs/apk/debug/app-debug.apk`).
- [ ] **Paso 8 — Comprobación en el teléfono y documentación.** `docs/manual-checklist.md`
  y `README.md` escritos. **Pendiente:** ejecutar la lista en el teléfono y anotar Q-SK1,
  Q-SK2 y Q-SK3.

## Resultados verificados

- `./gradlew test`: 46 pruebas (41 de `core`, 5 de `app`), 0 fallos.
- `./gradlew ktlintCheck detekt`: sin incidencias en `core` ni en `app`.
- `./gradlew :app:lintDebug`: 0 errores, 13 avisos (ícono de la app, API objetivo,
  sugerencias de KTX, recurso sin usar, reglas de extracción de datos); ninguno bloquea.
- `./gradlew :app:assembleDebug`: APK generado.

## Desviaciones respecto al plan

- La codificación estática usa `Bitmap.compress` de Android (formato WebP con pérdida)
  en lugar de `webp-android`: es nativa y suficiente para estáticos. `webp-android` se
  incorpora en U3 para los animados, como prevé ADR-003.
- El ícono del paquete lo genera un puerto propio de `core/pack` (`TrayIconRenderer`)
  en lugar de un método del codificador, para que las reglas de paquetes no dependan de
  la conversión.
- `compileSdk` 37 en lugar de 36: es la plataforma que instaló el asistente de Android
  Studio y Android Gradle Plugin 9.4 la admite. `targetSdk` sigue en 36.
- La regla de cobertura del 80 % del alcance `feature` no se aplica: la postura de
  pruebas de la persona fija "sin objetivo numérico".
