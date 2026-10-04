# Plan de generación de código — U3 — Conversión a sticker

> Plan que la persona aprueba antes de escribir código. Cambiarlo después de
> aprobado requiere volver a aprobarlo.

## Alcance

- **Unidad:** U3 — Conversión a sticker
- **Historias / requisitos cubiertos:** US3.1, US3.2 / FR4.1–FR4.6, FR7.2
- **Fuera de este plan:** paquetes por serie y resumen (U4), pantallas (U5),
  extracción (U2).

## Contexto técnico

- **Stack:** el de `project.md`; se empieza a usar `webp-android` 1.1.2.
- **Postura de pruebas:** mixta — en `core`, prueba primero; en adaptadores,
  código y después prueba.
- **Estrategia de pruebas:** standard.
- **Comando de pruebas de esta unidad:** `./gradlew :core:test` y
  `./gradlew :app:testDebugUnitTest`.
- **Rama:** `feat/u3-conversion`, creada desde `feat/u1-walking-skeleton` porque
  U1 aún no está unida a `main` (no hay repositorio remoto todavía).

## Línea base

- `./gradlew test`: 48 pruebas en verde (43 `core`, 5 `app`).
- **Radio de impacto:** medio.

| Archivo | Cambio | Consumidores / pruebas afectadas |
|---|---|---|
| `core/.../conversion/Conversion.kt` | `SourceImageInfo` con duraciones; puerto ampliado | `StickerConverterTest`, adaptadores |
| `core/.../conversion/StickerConverter.kt` | Camino animado | `StickerConverterTest`, pantalla del esqueleto |
| `app/.../conversion/*` | Decodificación y codificación animadas | `AppContainer` |
| `app/.../ui/skeleton/SkeletonViewModel.kt` | Usa la serie del sticker convertido | — |

## Pasos

- [ ] **Paso 1 — Tipos de `core` (prueba primero)** (FR4.4)
  - Pruebas: casos 1–5 del diseño funcional.
  - Archivos: `SourceImageInfo`, `SampledFrame`, `FrameSampler.kt`,
    `FrameBudget.kt`, `WebpSniffer.kt`.
  - Hecho cuando: las pruebas pasan.
- [ ] **Paso 2 — Conversor animado (prueba primero)** (FR4.4, FR4.5, BR-11)
  - Pruebas: casos 6–10.
  - Archivos: `AnimatedQualityLadder.kt`, `StickerConverter.kt`, puerto
    `StickerEncoder.encodeAnimated`.
  - Hecho cuando: las 48 pruebas anteriores y las nuevas pasan.
- [ ] **Paso 3 — Adaptadores de `app`** (FR4.4, NFR2, NFR-U3.1)
  - Archivos: `BitmapImageSource.kt`, `AnimatedDecodedImage.kt`,
    `WebpStickerEncoder.kt`, `AppContainer.kt`, `SkeletonViewModel.kt`.
  - Hecho cuando: `assembleDebug`, `ktlintCheck`, `detekt` y `lintDebug` sin
    errores.
- [ ] **Paso 4 — Comprobación en el teléfono**
  - Con la pantalla del esqueleto y un video con stickers animados: el sticker
    llega animado a "TikTok animados 1" en WhatsApp; tiempos en el registro.
  - Archivos: `docs/manual-checklist.md` (sección U3).
  - Hecho cuando: resultado anotado en el registro de avance.

## Riesgos y decisiones abiertas

- El comportamiento real de `WebPDecoder` (marcas de tiempo, cuadros compuestos)
  se confirma en el teléfono → si difiere, se ajusta solo el adaptador.
- El esqueleto necesita 3 stickers por paquete y ahora puede haber dos series →
  sigue repitiendo el sticker en la serie que corresponda.

## Trazabilidad

| Historia / requisito | Pasos | Pruebas |
|---|---|---|
| US3.1 · FR4.1–FR4.3, FR4.6 | 2, 3 | `StickerConverterTest` (caso 10) |
| US3.2 · FR4.4 | 1, 2, 3, 4 | `FrameSamplerTest`, `WebpSnifferTest`, `StickerConverterTest` (6, 7) |
| US3.2 · FR4.5 | 2, 4 | `StickerConverterTest` (8, 9) |
| NFR2, NFR-U3.1 | 1, 3, 4 | `FrameBudgetTest`; medición |
