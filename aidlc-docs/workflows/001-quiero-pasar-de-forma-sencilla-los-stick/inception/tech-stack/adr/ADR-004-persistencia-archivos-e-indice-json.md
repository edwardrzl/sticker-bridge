# ADR-004: Paquetes en archivos con índice JSON

## Estado
Aceptada (por delegación de la persona; pendiente de su revisión)

## Fecha
2026-10-02

## Contexto
La app guarda los archivos de los stickers y unos pocos datos por paquete:
identificador, nombre, tipo (animado o estático), lista ordenada de stickers,
número de versión que WhatsApp usa para detectar cambios y si ya fue agregado.
Son decenas de paquetes como mucho. WhatsApp lee los archivos a través de un
`ContentProvider`. Un cierre a mitad de operación no debe dejar un paquete
inválido (NFR9), y los datos nunca salen del teléfono.

## Decisión
Guardamos cada sticker e ícono como archivo en el almacenamiento interno de la
app, en una carpeta por paquete, y los metadatos de todos los paquetes en un
único archivo de índice JSON con número de versión de esquema. Cada operación
escribe primero los archivos nuevos y después reemplaza el índice de forma
atómica (escribir en un archivo temporal y renombrar). El índice es la fuente
de verdad: un archivo no referenciado en él no existe para la app y se limpia
al arrancar.

## Consecuencias

### Positivas
- Sin base de datos, esquema ni migraciones de SQL.
- El `ContentProvider` sirve los archivos tal como están.
- La atomicidad se reduce a un renombrado.

### Negativas
- Todo el índice se reescribe en cada cambio (irrelevante a este tamaño).
- Sin consultas; si en el futuro hay búsqueda o etiquetas, habrá que migrar.

### Neutras
- El almacenamiento interno se borra al desinstalar la app; los paquetes
  desaparecen de WhatsApp con ella.

## Alternativas consideradas

### Room (SQLite)
- Pros: transacciones, consultas.
- Contras: esquema y migraciones para una sola relación y pocos registros.

### Jetpack DataStore
- Pros: escritura atómica incluida.
- Contras: pensado para preferencias; no aporta frente a un JSON propio y
  añade una dependencia.

## Referencias
- `requirements.md` (FR5.1–FR5.7, NFR8, NFR9, NFR17)
