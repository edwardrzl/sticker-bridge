# ADR-005: Arquitectura de puertos y adaptadores sobre dos módulos

## Estado
Aceptada (por delegación de la persona; pendiente de su revisión)

## Fecha
2026-10-02

## Contexto
La app depende de tres sistemas que no controla y que pueden cambiar sin
aviso: la web de TikTok, el contrato de stickers de WhatsApp y una librería de
imágenes de un tercero. Las reglas del proyecto exigen que la extracción sea
reemplazable sin tocar el resto, que la lógica pura no dependa de Android ni
de TikTok, y que esa lógica se pruebe primero. El repositorio debe servir de
muestra de buena arquitectura. El proyecto es pequeño: pocas pantallas y un
solo flujo.

## Decisión
Organizamos el código en puertos y adaptadores. El módulo `core` (Kotlin
puro) contiene las reglas de negocio y los casos de uso, y declara una
interfaz (puerto) por cada necesidad externa: extraer imágenes, obtener y
codificar imágenes, persistir paquetes, publicar paquetes, registrar
diagnósticos. El módulo `app` contiene los adaptadores que implementan esos
puertos y la interfaz de usuario. `app` depende de `core`; `core` no depende
de `app` ni de Android. Las dependencias se arman a mano en un contenedor de
aplicación.

## Consecuencias

### Positivas
- Sustituir la extracción, la librería de WebP o el almacenamiento es escribir
  otro adaptador.
- Las reglas se prueban en la JVM con adaptadores falsos, sin emulador.
- El compilador impide que `core` use Android.

### Negativas
- Más interfaces y tipos de los que exigiría una app directa.
- Los datos cruzan una frontera: algunas conversiones entre tipos de Android y
  tipos de `core`.

### Neutras
- Las acciones que necesitan una pantalla activa (abrir la confirmación de
  WhatsApp, el selector de galería) se quedan en `app`; `core` solo indica qué
  acción corresponde.

## Alternativas consideradas

### Capas en un solo módulo
- Pros: menos configuración de Gradle.
- Contras: nada impide que la lógica importe clases de Android; las pruebas
  necesitarían el entorno de Android.

### Sin capas
- Pros: mínimo código.
- Contras: incumple las reglas del proyecto y deja la extracción entrelazada
  con las pantallas.

### Un módulo por funcionalidad
- Pros: aislamiento máximo.
- Contras: desproporcionado para una app de este tamaño.

## Referencias
- `aidlc-docs/memory/project.md` (reglas y estilo), `requirements.md` (NFR11)
- `components.md`
