# Instrucciones de pruebas — U1 — Esqueleto funcional

Cómo ejecutar las pruebas de esta unidad.

| Ámbito | Comando | Requiere |
|---|---|---|
| Lógica (`core`) | `./gradlew :core:test` | Java 17+ |
| Adaptadores en la JVM (`app`) | `./gradlew :app:testDebugUnitTest` | SDK de Android |
| Formato y análisis | `./gradlew ktlintCheck detekt` | Java 17+ |
| Lint de Android | `./gradlew :app:lintDebug` | SDK de Android |
| Comprobación en el teléfono | `docs/manual-checklist.md` | Teléfono con depuración USB |

Resultados de `core`: `core/build/test-results/test/`. Informe HTML:
`core/build/reports/tests/test/index.html`.
