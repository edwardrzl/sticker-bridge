package com.edrl.stickerbridge.core.pack

import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import java.util.UUID

/**
 * Owns the pack rules: which pack a sticker goes to, naming, versioning.
 *
 * Every operation reads the stored packs, computes the new state and commits it at once, so a
 * failure never leaves a half-applied change. Splitting a full pack (BR-14) arrives with unit U4;
 * until then a full pack simply opens the next one.
 */
class PackService(
    private val repository: PackRepository,
    private val trayIcons: TrayIconRenderer,
    private val ids: StickerIdGenerator = StickerIdGenerator { UUID.randomUUID().toString().replace("-", "") },
) {
    suspend fun listPacks(): List<StickerPack> = repository.load()

    /** Adds the sticker to the open pack of its series and returns that pack as stored. */
    suspend fun addSticker(converted: ConvertedSticker): StickerPack {
        val packs = repository.load()
        val id = ids.next()
        val sticker = Sticker(id, "$id.webp", PackNaming.DEFAULT_EMOJI, converted.file.sizeBytes)
        val open =
            packs
                .filter { it.series == converted.series }
                .maxByOrNull { it.number }
                ?.takeIf { it.stickers.size < StickerLimits.MAX_STICKERS }

        val updated: StickerPack
        val staged: List<StagedFile>
        if (open == null) {
            val number = (packs.filter { it.series == converted.series }.maxOfOrNull { it.number } ?: 0) + 1
            updated = newPack(converted.series, number, sticker)
            val trayIcon = trayIcons.render(converted.file)
            staged =
                listOf(
                    StagedFile(converted.file.path, updated.identifier, sticker.fileName),
                    StagedFile(trayIcon.path, updated.identifier, StickerPack.TRAY_ICON_FILE),
                )
        } else {
            updated = open.copy(stickers = open.stickers + sticker, imageDataVersion = open.imageDataVersion + 1)
            staged = listOf(StagedFile(converted.file.path, updated.identifier, sticker.fileName))
        }

        val newState = packs.filterNot { it.identifier == updated.identifier } + updated
        repository.commit(newState.sortedWith(compareBy({ it.series }, { it.number })), staged)
        return updated
    }

    private fun newPack(
        series: PackSeries,
        number: Int,
        first: Sticker,
    ) = StickerPack(
        identifier = PackNaming.identifier(series, number),
        name = PackNaming.name(series, number),
        publisher = PackNaming.PUBLISHER,
        series = series,
        number = number,
        imageDataVersion = 1,
        stickers = listOf(first),
    )
}
