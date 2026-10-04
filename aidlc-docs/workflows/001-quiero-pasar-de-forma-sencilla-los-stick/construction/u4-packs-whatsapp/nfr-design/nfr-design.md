# Diseño NFR — U4 — Paquetes y WhatsApp

| Requisito | Mecanismo | Dónde | Verificación |
|---|---|---|---|
| NFR9.2 | Commit en tres fases: copiar archivos nuevos o movidos → renombrado atómico del índice → borrar obsoletos (un fallo al borrar solo deja huérfanos inofensivos) | `FilePackRepository` | Prueba con archivos temporales |
| NFR10.3 | `PackValidator` antes de decidir `OpenWhatsApp` | `SaveStickersUseCase` | Prueba unitaria |
| NFR17.3 | Lista `obsolete` en cada commit; temporales de conversión movidos, no copiados | Repositorio, `PackService` | Prueba |
| NFR11.3 | Puertos `PackRepository`, `StickerPackPublisher`, `TrayIconRenderer` falsos en pruebas | `core` | Pruebas |

## Impacto en el plan de código

- Cambia el puerto `PackRepository.commit` (orígenes de archivo y obsoletos).
- Nuevo `SaveStickersUseCase` en `core/save`.
- La pantalla de prueba usa el caso de uso (se sustituye en U5).
