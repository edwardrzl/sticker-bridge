# Resumen de build y pruebas

Resultado de compilar el proyecto desde cero y ejecutar todas las comprobaciones, el
2026-10-06, sobre la rama `docs/build-and-test` (incluye U1–U5 y los pasos 9 a 11 de U5:
respuestas, filtro de descargas, diseño y build de publicación). Todos los números salen
de ejecuciones de esta etapa.

## Build

| Paso | Comando | Resultado |
|---|---|---|
| Limpiar | `./gradlew clean` | Correcto |
| APK de depuración | `./gradlew :app:assembleDebug` | Correcto, 18,4 MB |
| APK de publicación (R8) | `./gradlew :app:assembleRelease` | Correcto, 6,6 MB, sin firmar |
| Instalar en el teléfono | `./gradlew :app:installDebug` | Instalado en OPPO A78 (Android 15) |

Por la poca memoria de la computadora, los comandos se ejecutaron con
`--max-workers=1 -Pkotlin.compiler.execution.strategy=in-process`.

## Pruebas

| Capa | Comando | Pasan | Fallan | Omitidas | Cobertura |
|---|---|---|---|---|---|
| Unitarias `core` (17 clases) | `./gradlew :core:test` | 120 | 0 | 0 | No medida |
| Unitarias `app` (4 clases) | `./gradlew :app:testDebugUnitTest` | 26 | 0 | 0 | No medida |
| Capturas de pantalla (9) | `./gradlew :app:validateDebugScreenshotTest` | 9 | 0 | 0 | — |
| Formato | `./gradlew ktlintCheck` | Sin incidencias | | | |
| Análisis estático | `./gradlew detekt` | Sin incidencias | | | |
| Android Lint | `./gradlew :app:lintDebug` | 0 errores, 16 avisos | | | |

La cobertura de líneas no se mide: el proyecto no fijó objetivo numérico
(`project.md`, postura de pruebas) y no tiene herramienta de cobertura configurada.

No hay pruebas de integración entre unidades ni de interacción con la interfaz; lo que
depende de TikTok y WhatsApp reales se comprueba con `docs/manual-checklist.md`.

### Avisos de Android Lint que quedan

| Aviso | Cuántos | Qué es | Decisión |
|---|---|---|---|
| `Aligned16KB` | 3 | Las bibliotecas nativas de `webp-android` 1.1.2 no están alineadas a 16 KB | **Riesgo real**, ver abajo |
| `UseKtx` | 5 | Sugerencias de estilo | No se cambian: no afectan al comportamiento |
| `PluralsCandidate` | 4 | Textos con número que podrían ser plurales ("1 stickers") | Pendiente menor |
| `RequiresFeature` | 2 | Lint no ve que la función se comprueba antes en `loadInitial` | Falso positivo |
| `OldTargetApi` | 1 | `targetSdk` 36 con `compileSdk` 37 | Decisión de `tech-stack.md` |
| `ObsoleteSdkInt` | 1 | Carpeta `mipmap-anydpi-v26` | Se intentó quitar el sufijo y el build dejó de encontrar el icono; se deja |

## Correcciones realizadas en esta etapa

- Texto `search_likes` eliminado: quedó sin uso tras el rediseño (regresión de este
  workflow, paso 10).
- Reglas de copia de seguridad (`data_extraction_rules.xml`): nada de lo que guarda la
  app sale del teléfono, ni por copia en la nube ni por traspaso entre dispositivos.
  Android 12 o superior ignora `allowBackup="false"` para el traspaso (preexistente).

## Cobertura de requisitos

| FR/US | Pruebas | Estado |
|---|---|---|
| FR1.1 compartir desde TikTok | Manual | Sin resultado anotado |
| FR1.2 pegar enlace | `SearchViewModelTest`; manual | Cubierto; probado en el teléfono |
| FR1.3, FR1.4 reconocer enlaces | `PostLinkParserTest`, `SearchViewModelTest` | Cubierto |
| FR2.1 extraer en el teléfono | `CommentResponseParserTest`; manual | Cubierto; probado en el teléfono |
| FR2.2, FR6.3 carga inicial y espera | `InitialLoadPolicyTest` | Cubierto |
| FR2.3 cancelar | `SearchViewModelTest` | Cubierto |
| FR2.4 cargar más | `SearchViewModelTest` | Cubierto |
| FR2.5 sin duplicados | `CommentResponseParserTest`, `CommentImageOrderTest` | Cubierto |
| FR2.6, FR2.7 una carga, navegador oculto | Revisión de código | Sin prueba automática |
| FR2.8 orden por likes | `CommentImageOrderTest`, `SearchViewModelTest` | Cubierto |
| FR2.10 respuestas | `CommentResponseParserTest`, `ReplyQueueTest`, `SearchViewModelTest` | Cubierto; **sin probar en el teléfono** |
| FR3.1–FR3.4 selección | `SearchViewModelTest`; capturas | Cubierto |
| FR4.1–FR4.8 conversión | `StickerConverterTest`, `FitCalculatorTest`, `FrameBudgetTest`, `FrameSamplerTest`, `WebpSnifferTest` | Cubierto |
| FR5.1–FR5.3 series y límites | `PackServiceTest`, `PackValidatorTest` | Cubierto |
| FR5.4 | — | Sustituido por FR5.12 |
| FR5.5, FR5.6, FR5.9 acciones tras guardar | `SaveStickersUseCaseTest`, `SearchViewModelTest` | Cubierto |
| FR5.7 persistencia | `PackIndexCodecTest`, `FilePackRepositoryTest` | Cubierto |
| FR5.8 lista de paquetes | `PacksViewModelTest`; capturas | Cubierto |
| FR5.10 WhatsApp no instalado | `SaveStickersUseCaseTest` | Cubierto |
| FR5.11 quitar sticker | `PackServiceTest`, `PacksViewModelTest` | Cubierto; sin resultado manual anotado |
| FR5.12 paquete desde 1 sticker | `PackPaddingTest`, `PackValidatorTest`, `SaveStickersUseCaseTest` | Cubierto; probado en el teléfono |
| FR6.1, FR6.2 errores | `SearchViewModelTest` | Cubierto |
| FR6.4 un fallo no cierra la app | Revisión de código | Sin prueba automática |
| FR7.1–FR7.3 importar de galería | `SearchViewModelTest`; manual | Cubierto; sin resultado manual anotado |
| NFR5 solo dominios de TikTok | `HostAllowlistTest`, `AllowlistInterceptorTest` | Cubierto |
| NFR6 tráfico del navegador | `HostAllowlistTest` | Cubierto |
| NFR1–NFR3 tiempos | Manual | **Sin medir** (criterio de 5 videos pendiente) |

## Fallas preexistentes (no introducidas por este workflow)

Ninguna: el proyecto es nuevo.

## Riesgos y pendientes

- **Bibliotecas nativas sin alinear a 16 KB** (`webp-android` 1.1.2). En teléfonos con
  páginas de memoria de 16 KB la conversión de stickers puede no cargar, y Google Play
  exige esa alineación a las apps que apuntan a Android 15 o superior. No afecta al
  teléfono de la persona. Corregirlo es cambiar de versión o de biblioteca de WebP.
- **Sin probar en el teléfono**: respuestas (FR2.10), filtro de descargas, rediseño. La
  build que los incluye está instalada.
- **El APK de publicación solo se compiló.** R8 puede romper código al que se llega por
  nombre; hay que ejecutar la lista manual sobre el APK firmado.
- **Criterio de salida del alcance sin cerrar**: 5 videos reales en menos de 1 minuto
  cada uno, compartir desde TikTok, importar de galería y quitar un sticker.
- La app lee 60 comentarios al abrir y 20 más por cada "Cargar más".
- Los PR #7 a #10 están abiertos y apilados; no se unen hasta la prueba en el teléfono.
