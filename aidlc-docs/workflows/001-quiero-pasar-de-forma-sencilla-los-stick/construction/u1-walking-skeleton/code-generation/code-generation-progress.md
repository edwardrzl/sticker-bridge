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
- [ ] **Paso 6 — Adaptadores de `app`.** Código escrito y con formato y análisis en
  verde (commit `30936cb`). **Pendiente:** compilar y ejecutar `FilePackRepositoryTest`
  (5 pruebas) cuando esté instalado el SDK de Android.
- [ ] **Paso 7 — Pantalla de prueba y manifiesto.** Código escrito (commit `daa2c3c`).
  **Pendiente:** generar el APK de depuración.
- [ ] **Paso 8 — Comprobación en el teléfono y documentación.** `docs/manual-checklist.md`
  y `README.md` escritos. **Pendiente:** ejecutar la lista en el teléfono y anotar Q-SK1,
  Q-SK2 y Q-SK3.

## Resultados verificados

- `./gradlew :core:test`: 41 pruebas, 0 fallos.
- `./gradlew ktlintCheck detekt`: sin incidencias en `core` ni en `app`.

## Desviaciones respecto al plan

- La codificación estática usa `Bitmap.compress` de Android (formato WebP con pérdida)
  en lugar de `webp-android`: es nativa y suficiente para estáticos. `webp-android` se
  incorpora en U3 para los animados, como prevé ADR-003.
- El ícono del paquete lo genera un puerto propio de `core/pack` (`TrayIconRenderer`)
  en lugar de un método del codificador, para que las reglas de paquetes no dependan de
  la conversión.
- La regla de cobertura del 80 % del alcance `feature` no se aplica: la postura de
  pruebas de la persona fija "sin objetivo numérico".
