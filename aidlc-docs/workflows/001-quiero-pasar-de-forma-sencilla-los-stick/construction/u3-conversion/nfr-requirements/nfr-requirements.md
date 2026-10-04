# Requisitos no funcionales — U3 — Conversión a sticker

Metas no funcionales de la conversión completa y amenazas en su frontera
(archivos de imagen de terceros).

| ID | Hereda de | Categoría | Requisito | Meta | Verificación |
|---|---|---|---|---|---|
| NFR2.2 | NFR2 | Rendimiento | Conversión de un sticker animado de TikTok (≤ 3 s, ~500×500) | < 6 s | Medición en el teléfono |
| NFR2.3 | NFR2 | Rendimiento | Conversión estática | < 2 s (se mantiene de U1) | Medición en el teléfono |
| NFR3.2 | NFR3 | Rendimiento | Decodificación y codificación fuera del hilo de interfaz | Siempre en `Dispatchers.Default`/`IO` | Revisión de código |
| NFR-U3.1 | — | Fiabilidad | Memoria de los cuadros decodificados | ≤ 96 MB por imagen | Prueba unitaria de `FrameBudget` |
| NFR10.2 | NFR10 | Fiabilidad | Todo sticker producido cumple los límites de WhatsApp | Estático ≤ 100 KB; animado ≤ 500 KB, ≤ 10 s, cuadros ≥ 8 ms, 512×512 | Pruebas unitarias del conversor y del muestreo |
| NFR17.2 | NFR17 | Almacenamiento | Intentos rechazados y temporales de decodificación | Borrados al terminar cada conversión | Prueba unitaria (descartes) y revisión |

## Modelo de amenazas (STRIDE)

| Amenaza | Activo / frontera | Mitigación | Estado |
|---|---|---|---|
| Denegación: imagen con miles de cuadros o enorme | Decodificador | Límite de descarga de 10 MB (U1) y presupuesto de 96 MB de cuadros | Diseñada |
| Manipulación: archivo WebP malformado | Decodificador nativo (libwebp) | Errores de la librería se traducen a `UnsupportedImage`; nunca cierran la app | Diseñada |
| Divulgación: temporales accesibles por otras apps | Archivos de trabajo | Carpeta de caché privada de la app | Diseñada |

## Decisiones técnicas derivadas

- Se usa ya `com.aureusapps.android:webp-android` 1.1.2 (ADR-003), que estaba en
  el catálogo desde U1.

## Cobertura de NFR del proyecto

| NFR | Estado | Requisitos de la unidad |
|---|---|---|
| NFR2 | OK | NFR2.2, NFR2.3 |
| NFR3 | OK | NFR3.2 |
| NFR10 | OK (stickers) | NFR10.2 |
| NFR17 | OK | NFR17.2 |
| Resto | N/A: sin cambios en red, permisos ni almacenamiento persistente | — |
