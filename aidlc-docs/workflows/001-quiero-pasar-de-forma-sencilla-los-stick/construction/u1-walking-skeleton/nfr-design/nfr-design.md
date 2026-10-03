# Diseño NFR — U1 — Esqueleto funcional

Mecanismos concretos con los que el esqueleto cumple sus requisitos no
funcionales. Preparado por adelantado, a la espera de que la etapa se inicie.

| Requisito | Mecanismo | Dónde se implementa | Cómo se verifica |
|---|---|---|---|
| NFR1.1, NFR2.1 | Medición con `TimeSource.Monotonic` alrededor de cada tramo; resultado al diagnóstico y a la pantalla | `app/ui` (pantalla de prueba) | Registro en el teléfono |
| NFR3.1 | Corrutinas en `viewModelScope`; extracción en `Dispatchers.Main` (el WebView lo exige), descarga y codificación en `Dispatchers.IO`/`Default` | ViewModel de prueba, adaptadores | Comprobación manual |
| NFR4.1 | WebView creado por código, nunca añadido a la jerarquía visible; sin foco ni teclado | `app/extraction/WebViewCommentImageExtractor` | Revisión |
| NFR4.2 | `close()`: `CookieManager.removeAllCookies`, `WebStorage.deleteAllData`, `clearCache(true)`, `destroy()` | Mismo adaptador | Comprobación manual |
| NFR6.1 | `shouldInterceptRequest` devuelve respuesta vacía 403 si `HostAllowlist.isAllowed` es falso; la lista vive en `core` | Adaptador + `core/extraction/HostAllowlist` | Prueba unitaria |
| NFR8.1 | `<provider android:exported="true" android:readPermission="com.whatsapp.sticker.READ">` | `AndroidManifest.xml` | Revisión |
| NFR8.2 | `WebViewCompat.addWebMessageListener` con origen `https://www.tiktok.com`; solo se aceptan textos | Adaptador | Revisión |
| NFR9.1 | Escribir `packs.json.tmp`, `flush` y renombrado atómico (`Files.move` con `ATOMIC_MOVE`) | `app/pack/storage/FilePackRepository` | Prueba con archivos temporales |
| NFR10.1 | `PackValidator` antes de lanzar el intent y antes de exponer en el `ContentProvider` | `core/pack` | Prueba unitaria |
| NFR12.1 | `DiagnosticLog` con etiquetas fijas; nunca textos de comentarios ni usuarios | `core/diagnostics` + `app/di/AndroidDiagnosticLog` | Revisión |
| NFR-U1.1 | ktlint y detekt configurados en el build raíz | `build.gradle.kts` | `./gradlew ktlintCheck detekt` |

## Seguridad

- Permisos: solo `android.permission.INTERNET`.
- `ContentProvider` protegido por el permiso de WhatsApp; las rutas de archivos
  se validan contra la carpeta del paquete (sin `..`).
- `<queries>` con `com.whatsapp` para poder consultar a WhatsApp.
- Sin `addJavascriptInterface`; el canal acepta solo cadenas del origen de
  TikTok.
- Sin secretos: el APK de depuración usa la clave de depuración local, que
  nunca se versiona (`.gitignore`: `*.jks`, `*.keystore`, `local.properties`).

## Resiliencia

| Dependencia | Tiempo límite | Reintentos | Si falla |
|---|---|---|---|
| Página de TikTok (primera tanda) | 30 s | Ninguno automático (FR2.6) | `Timeout` o `CommentsBlocked` |
| Descarga de imagen | 15 s, máx. 10 MB | Ninguno | `DownloadFailed` |
| Consulta a WhatsApp (`isAdded`) | — | Ninguno | Se trata como no agregado |
| Escritura del índice | — | Ninguno | El índice anterior queda intacto |

## Observabilidad

Registro de Android con etiqueta `StickerBridge` y subetiquetas por
componente (`extraction`, `conversion`, `pack`, `whatsapp`). Campos: tramo,
duración en ms, conteos, tipo de error, dominio bloqueado. Sin métricas ni
trazas remotas (NFR5).

## Configuración

No hay variables de entorno: es una app móvil sin servidor. Constantes en
código: tiempos límite, lista de dominios, límites de WhatsApp. No hay
secretos y por tanto no hace falta `.env.example`.

## Impacto en el plan de código

- Añadir `androidx.webkit` al catálogo de versiones.
- Prueba unitaria de `HostAllowlist` y del guardado atómico.
- Manifiesto con permiso único, `<queries>` y proveedor protegido.
- Medición de tiempos en la pantalla de prueba.
