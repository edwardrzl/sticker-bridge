# ADR-003: Conversión a WebP con libwebp mediante `webp-android`

## Estado
Aceptada (por delegación de la persona; pendiente de su revisión)

## Fecha
2026-10-02

## Contexto
Cada imagen elegida debe convertirse a WebP de 512×512 dentro de límites
estrictos: estático ≤ 100 KB; animado ≤ 500 KB, ≤ 10 s y ≥ 8 ms por cuadro
(FR4). Hay que leer los cuadros de un WebP animado de origen, reescalarlos y
volver a codificarlos controlando calidad y cuadros por segundo. Android
decodifica WebP animado para mostrarlo, pero no expone sus cuadros de forma
directa ni trae un codificador de WebP animado. La conversión debe tardar
menos de 6 s por sticker animado en el teléfono (NFR2).

## Decisión
Usamos la librería `com.aureusapps.android:webp-android`, que envuelve libwebp
por JNI y permite tanto extraer los cuadros de un WebP animado como codificar
WebP estático y animado. Se usa para todos los stickers, estáticos y animados,
para tener un solo camino de codificación.

La librería queda detrás de una interfaz de codificación en `app/conversion`.
Las decisiones de conversión (tamaño de encaje, pasos de calidad y de cuadros,
cuándo caer a estático) se calculan en `core/conversion`, sin depender de ella.

## Consecuencias

### Positivas
- Sin NDK ni CMake en el entorno de desarrollo.
- Las reglas de FR4 se prueban en la JVM sin codificar imágenes reales.

### Negativas
- Dependencia de un proyecto de un solo mantenedor; puede quedar sin soporte.
- El peso final solo se conoce tras codificar: cumplir el límite puede exigir
  varias pasadas, lo que presiona NFR2.

### Neutras
- Si la librería no ofrece el control o el rendimiento necesarios, se
  sustituye la implementación de la interfaz por libwebp compilado con el NDK.

## Alternativas consideradas

### Compilar libwebp propio con el NDK y JNI
- Pros: control total, sin intermediario.
- Contras: añade NDK, CMake y código C al proyecto. Plan B.

### APIs de Android (`Bitmap.compress`, `ImageDecoder`)
- Pros: sin dependencias.
- Contras: solo WebP estático; no codifica animados.

### FFmpeg
- Contras: binario muy pesado para esta necesidad; `ffmpeg-kit` está retirado.

## Referencias
- https://github.com/UdaraWanasinghe/webp-android
- `requirements.md` (FR4.1–FR4.5, NFR2), `feasibility-assessment.md` (RSK-03, RSK-04)
