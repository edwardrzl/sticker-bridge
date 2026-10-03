# ADR-001: App Android nativa con Kotlin

## Estado
Aceptada (por delegación de la persona; pendiente de su revisión)

## Fecha
2026-10-02

## Contexto
La app debe extraer imágenes de comentarios de TikTok en el propio teléfono,
convertirlas a WebP (también animado) y entregarlas a WhatsApp como paquete de
stickers. Las tres piezas dependen de mecanismos nativos de Android: un
navegador interno con interceptación de peticiones, un proveedor de contenido
(`ContentProvider`) con el contrato que WhatsApp define, y una librería en C
(libwebp) para codificar WebP animado. El equipo es una persona, el costo debe
ser cero y el repositorio servirá como muestra de trabajo. No se ha
establecido experiencia previa de la persona en desarrollo móvil.

## Decisión
Construimos una app Android nativa en Kotlin, con Jetpack Compose para la
interfaz y Gradle como sistema de compilación, organizada en dos módulos:
`core` (Kotlin puro) y `app` (Android).

## Consecuencias

### Positivas
- Acceso directo a las tres piezas nativas, sin puentes.
- Un solo lenguaje y una sola cadena de herramientas.
- La lógica en `core` se prueba en la JVM en segundos, sin emulador.

### Negativas
- Solo Android; llevarla a iPhone exigiría reescribirla (iPhone es no-objetivo).
- Requiere instalar el SDK de Android.
- Si la persona no conoce Kotlin, hay curva de aprendizaje para mantenerla.

### Neutras
- La app se instala como APK de depuración; publicar en Google Play exigiría
  firma de publicación y revisar las políticas de la tienda.

## Alternativas consideradas

### React Native o Capacitor
- Pros: JavaScript, que la persona probablemente conoce (Node instalado).
- Contras: las tres piezas nativas habría que escribirlas igualmente en
  Kotlin, más el puente con JavaScript.

### Flutter
- Pros: buena interfaz multiplataforma.
- Contras: mismo problema de piezas nativas, y otro SDK que instalar.

### Web o PWA
- Descartada en factibilidad: no puede entregar animados ni paquetes.

## Referencias
- `feasibility-assessment.md`, `requirements.md` (FR2, FR4, FR5)
- https://github.com/WhatsApp/stickers/blob/main/Android/README.md
