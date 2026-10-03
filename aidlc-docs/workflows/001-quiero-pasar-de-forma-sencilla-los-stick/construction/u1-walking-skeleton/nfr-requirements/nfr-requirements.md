# Requisitos no funcionales — U1 — Esqueleto funcional

Metas no funcionales que aplican al esqueleto y amenazas en sus fronteras. El
esqueleto mide más de lo que garantiza: varias metas se registran aquí para
que U2 a U5 las cumplan con datos reales. Preparado por adelantado, a la
espera de que la etapa se inicie.

| ID | Hereda de | Categoría | Requisito | Meta | Verificación |
|---|---|---|---|---|---|
| NFR1.1 | NFR1 | Rendimiento | Medir el tiempo hasta la primera tanda de comentarios | Registrar; objetivo ≤ 15 s | Registro de depuración, 3 videos reales |
| NFR2.1 | NFR2 | Rendimiento | Conversión estática de una imagen | < 2 s | Registro de depuración |
| NFR3.1 | NFR3 | Rendimiento | Extracción, descarga y conversión fuera del hilo de interfaz | La pantalla responde durante todo el proceso | Comprobación manual |
| NFR4.1 | NFR4 | Privacidad | El WebView nunca muestra la página de inicio de sesión ni recibe credenciales | Siempre oculto y sin entrada de usuario | Revisión de código |
| NFR4.2 | NFR4 | Privacidad | Cookies y almacenamiento web borrados al cerrar cada sesión | Sin cookies de TikTok tras `close()` | Comprobación manual del almacén de cookies |
| NFR5.1 | NFR5 | Privacidad | Sin librerías de analítica, publicidad ni reporte de fallos | 0 dependencias de ese tipo | Revisión de `libs.versions.toml` |
| NFR6.1 | NFR6 | Privacidad | Peticiones del WebView limitadas a la lista permitida | Dominios ajenos bloqueados y registrados | Prueba unitaria de `HostAllowlist`; registro en el teléfono |
| NFR7.1 | NFR7 | Seguridad | Permisos declarados | Solo `INTERNET` | Revisión del manifiesto |
| NFR8.1 | NFR8 | Seguridad | `ContentProvider` exportado solo con el permiso de lectura de WhatsApp | `readPermission="com.whatsapp.sticker.READ"` | Revisión del manifiesto |
| NFR8.2 | — | Seguridad | El canal de mensajes del script inyectado solo acepta el origen de TikTok | Mensajes de otros orígenes ignorados | Revisión de código |
| NFR9.1 | NFR9 | Fiabilidad | Guardado atómico del índice | Fallo antes del renombrado deja el índice anterior | Prueba con archivos temporales |
| NFR10.1 | NFR10 | Fiabilidad | No se ofrece a WhatsApp un paquete inválido | Validación antes del intent | Prueba unitaria |
| NFR12.1 | NFR12 | Mantenibilidad | Diagnóstico de extracción sin datos personales | Solo tipo de fallo, dominio y extractos de estructura | Revisión de código |
| NFR13.1 | NFR13 | Compatibilidad | minSdk 26, targetSdk 36 | Instala en el teléfono de la persona | Instalación |
| NFR-U1.1 | — | Mantenibilidad | El proyecto compila y pasa formato y análisis desde el primer commit | `test`, `ktlintCheck`, `detekt` en verde | Comandos estándar |

## Modelo de amenazas (STRIDE)

| Amenaza | Activo / frontera | Mitigación | Estado |
|---|---|---|---|
| Suplantación: una página distinta de TikTok envía mensajes al canal de la app | Canal script → app | Canal registrado solo para el origen `https://www.tiktok.com` (NFR8.2) | Diseñada |
| Manipulación: respuesta de comentarios con datos inesperados o enormes | `CommentResponseParser` | Lectura tolerante, límites de tamaño, errores con tipo | Diseñada |
| Divulgación: otra app lee los stickers | `ContentProvider` | Permiso de lectura de WhatsApp (NFR8.1) | Diseñada |
| Divulgación: el WebView filtra datos a terceros | Peticiones del WebView | Lista de dominios permitidos (NFR6.1); sin sesión (NFR4) | Diseñada; se verifica en el teléfono |
| Denegación: descarga de una imagen enorme | Descarga | Tiempo límite 15 s y tamaño máximo de descarga 10 MB | Diseñada |
| Elevación: JavaScript de la página llama a la app | Interfaz expuesta al WebView | No se usa `addJavascriptInterface`; solo un canal de mensajes de texto | Diseñada |

## Decisiones técnicas derivadas

- `androidx.webkit` (biblioteca oficial de AndroidX) para el script de inicio
  de documento y el canal de mensajes restringido por origen. **Nueva frente a
  `tech-stack.md`**; es la forma soportada de cumplir NFR8.2 y NFR6.
- Límite de 10 MB por descarga de imagen.

## Cobertura de NFR del proyecto

| NFR | Estado | Requisitos de la unidad |
|---|---|---|
| NFR1 | Parcial: se mide | NFR1.1 |
| NFR2 | Parcial: solo estático | NFR2.1 |
| NFR3 | OK | NFR3.1 |
| NFR4 | OK | NFR4.1, NFR4.2 |
| NFR5 | OK | NFR5.1 |
| NFR6 | OK | NFR6.1 |
| NFR7 | OK | NFR7.1 |
| NFR8 | OK | NFR8.1, NFR8.2 |
| NFR9 | OK | NFR9.1 |
| NFR10 | Parcial: un paquete | NFR10.1 |
| NFR11 | OK por diseño | Puertos definitivos en U1 |
| NFR12 | OK | NFR12.1 |
| NFR13 | OK | NFR13.1 |
| NFR14, NFR15 | N/A: la pantalla de U1 es provisional | — |
| NFR16 | OK | Sin servicios de pago |
| NFR17 | Parcial | Temporales borrados tras convertir |
