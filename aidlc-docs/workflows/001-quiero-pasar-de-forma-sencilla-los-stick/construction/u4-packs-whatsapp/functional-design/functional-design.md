# Diseño funcional — U4 — Paquetes y WhatsApp

Reglas completas de los paquetes: dos series que crecen, reparto al llenarse,
quitar stickers, guardado atómico, y el caso de uso que guarda una selección de
imágenes y dice qué hacer con WhatsApp. Incorpora lo aprendido en el teléfono:
WhatsApp solo recarga un paquete agregado si la app reabre su pantalla (FR5.6).

## Alcance de la unidad

Historias: US3.3, US4.1, US4.2, US4.3, US4.5 y CAP-11 · Requisitos: FR4.7, FR4.8,
FR5.1–FR5.11, FR7.2 · Reglas: BR-11 a BR-22 · NFR9, NFR10, NFR17.

## Modelo de datos

Índice `packs.json` (esquema 1, sin cambios de formato). Cambia el puerto del
repositorio:

| Tipo | Cambio |
|---|---|
| `StagedFile` | Su origen es un temporal (`FileSource.Temp(path)`) o un archivo de otro paquete (`FileSource.InPack(pack, fileName)`), para el reparto |
| `PackFile` | Nuevo: `pack`, `fileName`; archivo a borrar tras el commit |
| `PackRepository.commit` | `commit(packs, staged, obsolete)`: copia los archivos nuevos, reemplaza el índice de forma atómica y **después** borra los obsoletos |

## Lógica de negocio

### OP-1 — Añadir un sticker (`PackService.addSticker`)

Como en U1, más el reparto (BR-14): si el paquete abierto de la serie tiene 30, se
crea el siguiente con los 2 últimos stickers del lleno (copiados, no movidos, y
borrados del lleno tras el commit) más el nuevo; el lleno queda con 28 y su
versión aumenta. Si el sticker que pasa a ser el primero cambia, se regenera el
ícono (BR-15). Devuelve los paquetes modificados (uno o dos).

### OP-2 — Quitar un sticker (`PackService.removeSticker`, FR5.11)

- Quita el sticker del paquete, aumenta la versión y marca su archivo como
  obsoleto.
- Si era el primero, regenera el ícono con el nuevo primero.
- Si el paquete queda vacío, se elimina del índice con su carpeta.
- Devuelve el paquete resultante (o nada si se eliminó).

### OP-3 — Guardar una selección (`SaveStickersUseCase.save`, FR4.8, FR5.5, FR5.9)

1. Convierte las imágenes de una en una (BR-23); una que falla se cuenta y se
   sigue (BR-22).
2. Añade cada convertida con OP-1.
3. Al final, por cada paquete afectado (sin repetir), decide la acción (BR-20,
   revisada por FR5.6):

| Estado del paquete | Acción |
|---|---|
| Menos de 3 stickers | `Waiting(pack, missing)` |
| 3 o más y válido | `OpenWhatsApp(pack)` (agregar o UPDATE: la misma pantalla) |
| Inválido | `Invalid(pack, violations)` (no debería ocurrir; se registra) |

4. Devuelve `SaveResult`: guardados por paquete, cuántos sin animación, cuántos
   fallaron, acciones y si WhatsApp está instalado (FR5.10).

## Interfaces

```kotlin
class PackService {
    suspend fun addSticker(converted: ConvertedSticker): List<StickerPack>
    suspend fun removeSticker(packIdentifier: String, stickerId: String): StickerPack?
    suspend fun listPacks(): List<StickerPack>
}
class SaveStickersUseCase(converter, packs, publisher) {
    suspend fun save(sources: List<ImageRef>, onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }): SaveResult
}
sealed interface PackAction { Waiting(pack, missing); OpenWhatsApp(pack); Invalid(pack, violations) }
data class SaveResult(saved: Map<String, Int>, withoutAnimation: Int, failed: Int, actions: List<PackAction>, whatsAppInstalled: Boolean)
```

## Validaciones y errores

| Caso | Respuesta |
|---|---|
| Fallo al guardar el índice | `StorageException`; el estado anterior queda intacto (BR-21) |
| Quitar un sticker que no existe | Sin cambios |
| Todas las imágenes fallan | `SaveResult` con `failed = n` y sin acciones |

## Casos de prueba derivados

1. Sticker 31 de una serie: segundo paquete con 3 (los 2 últimos del lleno + el
   nuevo), el primero con 28, ninguno perdido ni duplicado; obsoletos = los 2
   movidos del lleno.
2. El reparto aumenta la versión del lleno.
3. Quitar un sticker: desaparece, versión +1, archivo obsoleto.
4. Quitar el primero: se regenera el ícono.
5. Quitar el último: el paquete desaparece del índice.
6. Guardar 3 imágenes (2 animadas, 1 que falla): 2 guardadas en la serie animada,
   1 fallida, una acción por paquete afectado.
7. Paquete con 2 tras guardar → `Waiting(missing = 1)`; con 3 → `OpenWhatsApp`.
8. Un animado que cae a estático cuenta en `withoutAnimation` y va a la serie
   estática.
9. Repositorio de archivos: copia desde otro paquete, borra obsoletos solo tras el
   índice, y un fallo antes del índice no borra nada.

## Trazabilidad

| Historia / FR | Sección |
|---|---|
| US4.3 · FR5.2 | OP-1, casos 1–2 |
| CAP-11 · FR5.11 | OP-2, casos 3–5 |
| US3.3, US4.1, US4.2 · FR4.8, FR5.5, FR5.6, FR5.9 | OP-3, casos 6–8 |
| NFR9 | Commit en tres fases, caso 9 |
