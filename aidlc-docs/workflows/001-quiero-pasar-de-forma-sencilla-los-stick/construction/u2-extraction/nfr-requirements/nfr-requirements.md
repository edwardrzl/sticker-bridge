# Requisitos no funcionales — U2 — Enlaces y extracción

| ID | Hereda de | Categoría | Requisito | Meta | Verificación |
|---|---|---|---|---|---|
| NFR1.2 | NFR1 | Rendimiento | Carga inicial completa (hasta 3 tandas) | ≤ 15 s con wifi | Medición en el teléfono (hoy 11–15 s) |
| NFR1.3 | NFR1 | Rendimiento | "Cargar más" | Una tanda en ≤ 5 s | Medición en el teléfono |
| NFR4.3 | NFR4 | Privacidad | Sin sesión de TikTok | Cookies y almacenamiento borrados al cerrar la sesión (U1) | Revisión |
| NFR6.2 | NFR6 | Privacidad | Solo dominios de TikTok | Lista permitida (U1) | Prueba unitaria |
| NFR11.2 | NFR11 | Mantenibilidad | Reglas de extracción en `core` | Enlaces, orden y política de carga con pruebas en la JVM | Pruebas unitarias |
| NFR12.2 | NFR12 | Mantenibilidad | Diagnóstico de fallos | Cada `fail:` del script queda en el registro con su motivo | Revisión |

## Modelo de amenazas (STRIDE)

| Amenaza | Activo / frontera | Mitigación | Estado |
|---|---|---|---|
| Manipulación: texto compartido malicioso | `PostLinkParser` | Solo se aceptan las formas listadas; la dirección se reconstruye, no se usa tal cual | Diseñada |
| Suplantación: mensajes de otra página | Canal script → app | Canal restringido a `https://www.tiktok.com` (U1) | Hecha |
| Denegación: "cargar más" sin fin | `loadMore` | Una tanda por pulsación; nada en segundo plano (FR2.6) | Diseñada |

## Cobertura de NFR del proyecto

| NFR | Estado |
|---|---|
| NFR1, NFR4, NFR6, NFR11, NFR12 | OK (tabla) |
| Resto | N/A: sin cambios |
