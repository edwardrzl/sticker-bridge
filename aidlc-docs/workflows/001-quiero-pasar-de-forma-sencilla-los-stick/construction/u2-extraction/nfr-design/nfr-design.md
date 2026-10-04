# Diseño NFR — U2 — Enlaces y extracción

| Requisito | Mecanismo | Dónde | Verificación |
|---|---|---|---|
| NFR1.2 | Espera fija del script de 2 s → 1 s tras `load`; reintentos a 3 s | `comment-capture.js` | Medición |
| NFR1.3 | `loadMore` pide una sola página y espera hasta 15 s | Extractor | Medición |
| NFR11.2 | `PostLinkParser`, `CommentImageOrder`, `InitialLoadPolicy` en `core/extraction` y `core/link` | `core` | Pruebas |
| NFR12.2 | Mensaje `fail:<motivo>` del script → `DiagnosticLog` | Extractor | Revisión |

## Resiliencia

| Dependencia | Fallo | Respuesta |
|---|---|---|
| Petición de comentarios | Estado distinto de 0 o no JSON | 2 reintentos (3 s); después `fail:` → `UnexpectedFormat` |
| Página cerrada a mitad | Respuesta indefinida | El script lo ignora sin errores |

## Impacto en el plan de código

- Nuevas clases de `core` con sus pruebas; el adaptador usa `InitialLoadPolicy`.
- Script: guarda el `cursor`, expone `__stickerBridgeLoadMore`, envía `fail:`.
