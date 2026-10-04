# Avance del plan — U1 — Esqueleto funcional

Copia de los pasos del plan aprobado con su estado real (el plan aprobado no se
modifica). Rama: `feat/u1-walking-skeleton`.

- [x] **Paso 1 — Proyecto y herramientas.** Gradle 9.8.0 (wrapper), AGP 9.4.1, Kotlin
  2.4.20, catálogo de versiones, ktlint 14.2.0, detekt 1.23.8. Commit `c0e128a`.
  `app` configura pero no compila: falta el SDK de Android.
- [x] **Paso 2 — Tipos y puertos de `core`.** Commit `92f06d2`.
- [x] **Paso 3 — Lectura de respuestas y dominios.** 12 pruebas. Commit `8d25cbd`.
- [x] **Paso 4 — Encaje y calidad estática.** 11 pruebas. Commit `cf89270`.
- [x] **Paso 5 — Reglas mínimas de paquetes.** 18 pruebas. Commit `7366ab8`.
- [x] **Paso 6 — Adaptadores de `app`.** Commits `10dfc5f` y `e0887d4` (correcciones de
  compilación y fallo del renderizador del navegador). `FilePackRepositoryTest`: 5 pruebas
  en verde.
- [x] **Paso 7 — Pantalla de prueba y manifiesto.** Commit `2340b7d`. APK de depuración
  generado (`app/build/outputs/apk/debug/app-debug.apk`).
- [x] **Paso 8 — Comprobación en el teléfono y documentación.** Ejecutada el 2026-10-03
  en un OPPO A78 (CPH2565, Android 15). Resultados abajo. Corrección necesaria: commit
  `b104051` (petición de comentarios desde la página y lectura de `cmt_sticker_struct`).

## Resultados en el teléfono

| Pregunta | Resultado |
|---|---|
| Q-SK1 — Extracción sin sesión | **Sí.** 2 imágenes encontradas con 2 videos reales, sin iniciar sesión; solo se bloqueó `accounts.google.com`, sin efecto. Primer intento fallido: la página ya no pide comentarios sola (ver enmienda de ADR-002). |
| Q-SK2 — Sticker nuevo visible sin volver a agregar | Pendiente de confirmar por la persona (cuenta de stickers tras "Añadir otro"). |
| Q-SK3 — Tiempos | Extracción 17,9 s desde el toque hasta las imágenes (meta ≤ 15 s para la primera tanda: se ajusta en U2, hay 2 s de espera fija y reintentos). Conversión 1,1 s (meta < 2 s). Sticker de 28 KB (límite 100 KB). |
| Paquete en WhatsApp | WhatsApp abrió su confirmación y aceptó el paquete; la persona usó los stickers en un chat. |

## Observaciones de la persona para unidades siguientes

- Solo se convierte una imagen por búsqueda: es el comportamiento provisional del
  esqueleto; la cuadrícula llega en U5.
- Apareció una imagen (una moto) que la persona no vio en los comentarios: la app revisa
  60 comentarios e incluye fotos, además de stickers.
- El orden no coincide con los comentarios más populares: propuesta para U2, ordenar por
  número de likes (`digg_count`).

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
