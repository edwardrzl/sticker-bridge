# Requisitos no funcionales — U4 — Paquetes y WhatsApp

| ID | Hereda de | Categoría | Requisito | Meta | Verificación |
|---|---|---|---|---|---|
| NFR9.2 | NFR9 | Fiabilidad | Añadir, repartir y quitar son atómicos | Un fallo antes del índice deja el estado anterior; los obsoletos solo se borran después | Pruebas unitarias |
| NFR10.3 | NFR10 | Fiabilidad | Solo se ofrecen paquetes válidos | Acción `Invalid` en lugar de abrir WhatsApp | Prueba unitaria |
| NFR17.3 | NFR17 | Almacenamiento | Sin archivos huérfanos acumulados | Obsoletos borrados tras cada commit | Prueba con archivos temporales |
| NFR11.3 | NFR11 | Mantenibilidad | Caso de uso sin Android | `SaveStickersUseCase` en `core` con puertos falsos | Pruebas unitarias |

## Modelo de amenazas (STRIDE)

| Amenaza | Activo / frontera | Mitigación | Estado |
|---|---|---|---|
| Manipulación: nombres de archivo con `..` | Repositorio de archivos | Nombres validados (U1) | Hecha |
| Pérdida de datos: cierre a mitad del reparto | Índice y archivos | Copiar → índice atómico → borrar | Diseñada |

## Cobertura de NFR del proyecto

| NFR | Estado |
|---|---|
| NFR9, NFR10, NFR11, NFR17 | OK (tabla) |
| Resto | N/A: sin cambios |
