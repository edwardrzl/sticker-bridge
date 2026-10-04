# Requisitos no funcionales — U5 — Interfaz completa

| ID | Hereda de | Categoría | Requisito | Meta | Verificación |
|---|---|---|---|---|---|
| NFR1.4 | NFR1 | Rendimiento | Flujo completo con paquete ya agregado | < 60 s del enlace al sticker en WhatsApp (criterio de salida) | Cronómetro, 5 videos reales |
| NFR3.3 | NFR3 | Rendimiento | La cuadrícula no se bloquea al cargar miniaturas | Miniaturas en segundo plano, con caché | Comprobación manual |
| NFR14.2 | NFR14 | Usabilidad | Pasos tras recibir el video | Elegir, guardar y una confirmación de WhatsApp por paquete | Comprobación manual |
| NFR15.2 | NFR15 | Accesibilidad | Zonas táctiles y etiquetas | ≥ 48 dp; descripción en miniaturas ("Sticker con N likes, elegido"); tema claro u oscuro del sistema | Revisión y lint |
| NFR7.2 | NFR7 | Seguridad | Galería sin permiso de almacenamiento | Selector de fotos del sistema | Revisión del manifiesto |

## Modelo de amenazas (STRIDE)

| Amenaza | Activo / frontera | Mitigación | Estado |
|---|---|---|---|
| Manipulación: texto compartido malicioso | Intent de Compartir | Solo `PostLinkParser` decide; el texto no se ejecuta ni se abre | Diseñada |
| Denegación: miniaturas enormes | Cargador de miniaturas | Límite de 10 MB y reducción al decodificar | Diseñada |

## Cobertura de NFR del proyecto

| NFR | Estado |
|---|---|
| NFR1, NFR3, NFR7, NFR14, NFR15 | OK (tabla) |
| Resto | Sin cambios |
