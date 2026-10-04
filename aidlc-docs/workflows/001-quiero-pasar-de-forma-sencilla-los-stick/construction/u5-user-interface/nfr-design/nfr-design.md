# Diseño NFR — U5 — Interfaz completa

| Requisito | Mecanismo | Dónde | Verificación |
|---|---|---|---|
| NFR3.3 | `ThumbnailLoader`: OkHttp con `Referer` de TikTok, `BitmapFactory` con `inSampleSize` a ~256 px, `LruCache` en memoria; carga en `LaunchedEffect` | `app/ui/thumbnails` | Manual |
| NFR15.2 | `Modifier.minimumInteractiveComponentSize`, `contentDescription` en cada miniatura, `MaterialTheme` con esquemas claro/oscuro | Pantallas | Revisión |
| NFR7.2 | `ActivityResultContracts.PickMultipleVisualMedia` | P1 | Manifiesto sin permisos nuevos |
| FR6.4 | Toda llamada de extracción y guardado captura los resultados tipados; `StorageException` se muestra como mensaje | ViewModels | Pruebas |

## Impacto en el plan de código

- Tres ViewModels con dependencias por interfaz para poder probarlos en la JVM.
- `MainActivity` recibe `ACTION_SEND` (`singleTop`, `onNewIntent`).
- La pantalla de prueba se elimina.
