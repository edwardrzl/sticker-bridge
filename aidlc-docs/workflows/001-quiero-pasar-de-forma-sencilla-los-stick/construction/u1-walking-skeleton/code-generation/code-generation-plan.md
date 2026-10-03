# Plan de generación de código — U1 — Esqueleto funcional

> Plan que la persona aprueba antes de escribir código. Cambiarlo después de
> aprobado requiere volver a aprobarlo.

Es el esqueleto funcional: la rebanada más fina de punta a punta (enlace →
extracción → conversión → paquete → WhatsApp) con una prueba real por capa,
para demostrar que las piezas conectan antes de las funcionalidades reales.
Preparado por adelantado, a la espera de que la etapa se inicie.

## Alcance

- **Unidad:** U1 — Esqueleto funcional
- **Historias / requisitos cubiertos:** viabilidad de US2.1, US3.1, US4.1,
  US4.2 / FR2.1, FR2.7, FR4.1–FR4.3, FR4.7, FR5.3, FR5.5–FR5.7 (mínimos)
- **Fuera de este plan:** animados, cargar más, errores detallados, varias
  series, reparto al llenarse, cuadrícula, Compartir, importación, pantallas
  definitivas.

## Contexto técnico

- **Stack:** Kotlin 2.4.20, Android Gradle Plugin 9.4.0, Gradle (wrapper),
  Compose BOM 2026.09.00, minSdk 26, targetSdk 36; módulos `core` (JVM) y
  `app`.
- **Postura de pruebas:** mixta — en `core` se escribe primero la prueba y
  después el código; en adaptadores y pantalla, código y después prueba.
- **Estrategia de pruebas:** standard (5–8 pruebas por componente en lo que
  toca la unidad). El piso de 80 % de cobertura del alcance `feature` choca
  con la postura acordada ("sin objetivo numérico"); se sigue la postura de la
  persona y se informa la cobertura sin imponer un mínimo.
- **Comando de pruebas de esta unidad:** `./gradlew :core:test` (lógica) y
  `./gradlew :app:testDebugUnitTest` (adaptadores en la JVM).

## Pasos

- [ ] **Paso 1 — Proyecto y herramientas**
  - Archivos: `settings.gradle.kts`, `build.gradle.kts`,
    `gradle/libs.versions.toml`, `gradle.properties`, wrapper de Gradle,
    `core/build.gradle.kts`, `app/build.gradle.kts`, `.gitignore`,
    `.editorconfig`, `config/detekt/detekt.yml`, `README.md`.
  - Hecho cuando: `./gradlew :core:test` corre (sin pruebas) y `ktlintCheck`
    y `detekt` pasan. `app` compila cuando el SDK de Android esté instalado.
- [ ] **Paso 2 — Tipos y puertos de `core`** (interfaces.md)
  - Archivos: `core/.../extraction/*`, `conversion/*`, `pack/*`,
    `diagnostics/DiagnosticLog.kt`.
  - Hecho cuando: compila; los puertos coinciden con `interfaces.md`.
- [ ] **Paso 3 — Lectura de respuestas y dominios (prueba primero)** (FR2.1,
  NFR6)
  - Pruebas: casos 1–4 del diseño funcional, con una respuesta de ejemplo en
    `core/src/test/resources`.
  - Archivos: `CommentResponseParser.kt`, `HostAllowlist.kt`.
  - Hecho cuando: las pruebas pasan.
- [ ] **Paso 4 — Encaje y calidad estática (prueba primero)** (FR4.1–FR4.3)
  - Pruebas: casos 5–6.
  - Archivos: `FitCalculator.kt`, `StaticQualityLadder.kt`,
    `StickerConverter.kt` (solo estático).
  - Hecho cuando: las pruebas pasan.
- [ ] **Paso 5 — Reglas mínimas de paquetes (prueba primero)** (FR4.7, FR5.3,
  FR5.6, NFR9, NFR10)
  - Pruebas: casos 7–10, con repositorio y publicador falsos.
  - Archivos: `StickerPack.kt`, `PackService.kt`, `PackValidator.kt`.
  - Hecho cuando: las pruebas pasan.
- [ ] **Paso 6 — Adaptadores de `app`** (FR2.1, FR2.7, FR4, FR5.7)
  - Archivos: `WebViewCommentImageExtractor.kt` y script inyectado,
    `OkHttpImageSource.kt`, `WebpStickerEncoder.kt`, `FilePackRepository.kt`,
    `StickerContentProvider.kt`, `WhatsAppPublisher.kt`,
    `AndroidDiagnosticLog.kt`, `AppContainer.kt`.
  - Pruebas: caso 11 (repositorio de archivos en la JVM).
  - Hecho cuando: `app` compila y la prueba pasa.
- [ ] **Paso 7 — Pantalla de prueba y manifiesto** (FR5.5)
  - Archivos: `MainActivity.kt`, `SkeletonViewModel.kt`, `SkeletonScreen.kt`,
    `AndroidManifest.xml`, `strings.xml` (español).
  - Hecho cuando: el APK de depuración se genera.
- [ ] **Paso 8 — Comprobación en el teléfono y documentación**
  - Archivos: `docs/manual-checklist.md` (Q-SK1, Q-SK2, Q-SK3), README con
    requisitos del entorno y advertencia sobre TikTok.
  - Hecho cuando: la persona ejecuta la lista en su teléfono y se anotan los
    resultados.

Cada paso termina en un commit (Conventional Commits) en la rama
`feat/u1-walking-skeleton`; al final, pull request a `main`.

## Riesgos y decisiones abiertas

- Sin SDK de Android instalado no se pueden compilar `app` ni ejecutar sus
  pruebas → los pasos 1–5 avanzan solo con Java; los pasos 6–8 esperan al SDK.
- El wrapper de Gradle necesita descargar Gradle una vez → requiere conexión.
- Sin repositorio remoto → los commits quedan locales hasta que exista.
- La forma real de la respuesta de comentarios puede diferir de la supuesta →
  el paso 8 guarda una respuesta real anonimizada y se ajusta el lector.
- `webp-android` puede no estar en la versión esperada → se fija la última
  estable al resolver dependencias.

## Trazabilidad

| Historia / requisito | Pasos | Pruebas |
|---|---|---|
| US2.1 · FR2.1, FR2.7, NFR6 | 3, 6, 8 | `CommentResponseParserTest`, `HostAllowlistTest`; Q-SK1 |
| US3.1 · FR4.1–FR4.3 | 4, 6 | `FitCalculatorTest`, `StaticQualityLadderTest` |
| US4.1 · FR4.7, FR5.3, FR5.5, FR5.7 | 5, 6, 7 | `PackServiceTest`, `PackValidatorTest`, `FilePackRepositoryTest` |
| US4.2 · FR5.6 | 5, 8 | `PackServiceTest`; Q-SK2 |
| NFR1, NFR2 | 7, 8 | Q-SK3 |
