# Avance del plan — U3 — Conversión a sticker

Copia de los pasos del plan aprobado con su estado real. Rama: `feat/u3-conversion`.

- [x] **Paso 1 — Tipos de `core`.** `SourceImageInfo` con duraciones, `SampledFrame`,
  `FrameSampler`, `FrameBudget`, `WebpSniffer`. Commit `a909aee`.
- [x] **Paso 2 — Conversor animado.** `AnimatedQualityLadder` y camino animado en
  `StickerConverter`. Commit `a909aee`.
- [x] **Paso 3 — Adaptadores de `app`.** `AnimatedWebpDecoder`, `DecodedImages`,
  `WebpStickerEncoder` animado. Commit `8426666`.
- [ ] **Paso 4 — Comprobación en el teléfono.** Conversión verificada (tabla abajo).
  **Pendiente:** que la persona confirme que los stickers se mueven en WhatsApp.

## Resultados verificados

- `./gradlew test`: 66 pruebas (61 `core`, 5 `app`), 0 fallos.
- `./gradlew ktlintCheck detekt :app:lintDebug`: sin incidencias nuevas.

| Imagen (video de prueba) | Resultado | Tamaño | Tiempo |
|---|---|---|---|
| Sticker animado, 19 cuadros, 1,9 s | Animado al primer intento (calidad 75) | 144 KB | 2,2 s |
| Sticker animado, 32 cuadros, 4,5 s | Animado al primer intento | 163 KB | 5,4 s |
| Sticker animado, 14 cuadros, 0,5 s | Animado al primer intento | 165 KB | 1,5 s |
| Foto | Estático | 30 KB | 1,1 s |
| Foto | Estático | 11 KB | 0,6 s |

Todos dentro de NFR2 (< 6 s) y de los límites de WhatsApp. WhatsApp abrió su
confirmación para "TikTok animados 1" (paquete válido).

## Desviaciones respecto al plan

- **OkHttp 4.12.0 en lugar de 5.x.** `webp-android` 1.1.2 depende de clases internas de
  OkHttp 4 y la app se cerraba al decodificar el primer animado con OkHttp 5. Cambio en
  el catálogo; `tech-stack.md` decía 5.x.
- **Presupuesto de memoria por número de cuadros** (`FrameBudget.keepEvery`) en lugar de
  por cadencia: las duraciones solo se conocen al decodificar.
- **Adelantado de U2:** la extracción recoge hasta 3 tandas o 10 s tras la primera
  (BR-03), necesario para encontrar stickers en la prueba. U2 lo formaliza en `core` con
  pruebas.
- **Pantalla de prueba:** convierte hasta 5 de las imágenes encontradas, para poder
  probar animados sin la cuadrícula de U5.
