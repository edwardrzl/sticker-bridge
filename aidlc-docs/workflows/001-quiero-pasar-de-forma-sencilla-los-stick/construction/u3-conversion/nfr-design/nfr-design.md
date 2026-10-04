# Diseño NFR — U3 — Conversión a sticker

Mecanismos con los que la conversión completa cumple sus metas.

| Requisito | Mecanismo | Dónde se implementa | Cómo se verifica |
|---|---|---|---|
| NFR2.2 | Escalera corta de 7 intentos; método de compresión 4 de libwebp (equilibrio velocidad/tamaño); cuadros ya escalados al decodificar | `core/conversion/AnimatedQualityLadder`, `app/conversion/WebpStickerEncoder` | Tiempos en el registro |
| NFR2.3 | Sin cambios (U1) | — | — |
| NFR3.2 | `withContext(Dispatchers.Default)` en codificación y escalado; `Dispatchers.IO` en lectura | Adaptadores de conversión | Revisión |
| NFR-U3.1 | `FrameBudget` decide la cadencia de decodificación; cada cuadro se escala y se libera el de origen | `core/conversion/FrameBudget`, `BitmapImageSource` | Prueba unitaria |
| NFR10.2 | `FrameSampler` garantiza ≥ 8 ms y duración total; límites de `StickerLimits` | `core/conversion` | Pruebas unitarias |
| NFR17.2 | `discard` de cada intento rechazado; borrado del temporal de decodificación en `close()` | Conversor y `AnimatedDecodedImage` | Prueba unitaria |

## Resiliencia

| Dependencia | Fallo | Respuesta |
|---|---|---|
| `WebPDecoder` | Resultado distinto de éxito o excepción | `UnsupportedImage` y registro de diagnóstico |
| `WebPAnimEncoder` | Excepción al ensamblar | El intento cuenta como fallido; se pasa al siguiente y, si todos fallan, caída a estático |

## Observabilidad

Registro `conversion`: número de cuadros, duración, cadencia elegida, intentos y
tamaño final, tiempo total. Sin datos personales.

## Configuración

Ninguna nueva. Constantes en `core`: presupuesto de 96 MB, intentos de la escalera.

## Impacto en el plan de código

- Ampliar el puerto `StickerEncoder` y `SourceImageInfo`.
- Pruebas de `FrameSampler`, `FrameBudget`, `WebpSniffer` y del conversor animado.
- `WebpStickerEncoder` necesita `Context` (lo exige la librería).
