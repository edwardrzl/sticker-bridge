package com.edrl.stickerbridge.core.pack

import com.edrl.stickerbridge.core.conversion.ConvertedSticker
import java.util.UUID

/** The pack a new sticker went to, and every pack the operation changed (two when a pack was split). */
data class AddResult(
    val target: StickerPack,
    val changed: List<StickerPack>,
)

/**
 * Owns the pack rules: which pack a sticker goes to, splitting full packs, removing stickers,
 * naming and versioning.
 *
 * Every operation reads the stored packs, computes the new state and commits it at once, so a
 * failure never leaves a half-applied change (BR-21).
 */
class PackService(
    private val repository: PackRepository,
    private val trayIcons: TrayIconRenderer,
    private val ids: StickerIdGenerator = StickerIdGenerator { UUID.randomUUID().toString().replace("-", "") },
) {
    suspend fun listPacks(): List<StickerPack> = repository.load()

    /** Adds the sticker to the open pack of its series, splitting it first if it is full (BR-13, BR-14). */
    suspend fun addSticker(converted: ConvertedSticker): AddResult {
        val packs = repository.load()
        val id = ids.next()
        val sticker = Sticker(id, "$id.webp", PackNaming.DEFAULT_EMOJI, converted.file.sizeBytes)
        val newFile = FileSource.Temp(converted.file.path)
        val open = packs.filter { it.series == converted.series }.maxByOrNull { it.number }
        val change =
            when {
                open == null -> newPack(converted.series, 1, listOf(sticker), newFile, firstSource = newFile)
                open.stickers.size < StickerLimits.MAX_STICKERS -> append(open, sticker, newFile)
                else -> split(open, sticker, newFile)
            }
        commit(packs, change)
        return AddResult(target = change.changed.last(), changed = change.changed)
    }

    /**
     * Removes a sticker (FR5.11). Returns the pack as stored, null when the pack is gone (its last
     * sticker was removed) or unknown.
     */
    suspend fun removeSticker(
        packIdentifier: String,
        stickerId: String,
    ): StickerPack? {
        val packs = repository.load()
        val pack = packs.firstOrNull { it.identifier == packIdentifier }
        val sticker = pack?.stickers?.firstOrNull { it.id == stickerId }
        return when {
            pack == null || sticker == null -> pack
            pack.stickers.size == 1 -> deletePack(packs, pack).let { null }
            else -> dropSticker(packs, pack, sticker)
        }
    }

    private suspend fun deletePack(
        packs: List<StickerPack>,
        pack: StickerPack,
    ) {
        val retired =
            (pack.stickers.map { it.fileName } + StickerPack.TRAY_ICON_FILE).map {
                PackFile(
                    pack.identifier,
                    it,
                )
            }
        repository.commit(packs - pack, emptyList(), retired)
    }

    private suspend fun dropSticker(
        packs: List<StickerPack>,
        pack: StickerPack,
        sticker: Sticker,
    ): StickerPack {
        val updated = pack.copy(stickers = pack.stickers - sticker, imageDataVersion = pack.imageDataVersion + 1)
        val newFirst = FileSource.InPack(updated.identifier, updated.stickers.first().fileName)
        val staged = if (sticker == pack.stickers.first()) listOf(trayFor(updated, newFirst)) else emptyList()
        commit(packs, Change(listOf(updated), staged, listOf(PackFile(pack.identifier, sticker.fileName))))
        return updated
    }

    private data class Change(
        val changed: List<StickerPack>,
        val staged: List<StagedFile>,
        val obsolete: List<PackFile> = emptyList(),
    )

    private fun append(
        open: StickerPack,
        sticker: Sticker,
        file: FileSource,
    ): Change {
        val updated = open.copy(stickers = open.stickers + sticker, imageDataVersion = open.imageDataVersion + 1)
        return Change(listOf(updated), listOf(StagedFile(file, updated.identifier, sticker.fileName)))
    }

    /** The full pack keeps 28 stickers; the next one starts with its last two plus the new one. */
    private suspend fun split(
        full: StickerPack,
        sticker: Sticker,
        file: FileSource,
    ): Change {
        val moved = full.stickers.takeLast(MOVED_ON_SPLIT)
        val kept =
            full.copy(
                stickers = full.stickers.dropLast(MOVED_ON_SPLIT),
                imageDataVersion =
                    full.imageDataVersion + 1,
            )
        val next =
            newPack(
                full.series,
                full.number + 1,
                moved + sticker,
                file,
                firstSource = FileSource.InPack(full.identifier, moved.first().fileName),
            )
        val movedFiles =
            moved.map {
                StagedFile(
                    FileSource.InPack(full.identifier, it.fileName),
                    next.changed.single().identifier,
                    it.fileName,
                )
            }
        return Change(
            changed = listOf(kept) + next.changed,
            staged = movedFiles + next.staged,
            obsolete = moved.map { PackFile(full.identifier, it.fileName) },
        )
    }

    private suspend fun newPack(
        series: PackSeries,
        number: Int,
        stickers: List<Sticker>,
        newFile: FileSource,
        firstSource: FileSource,
    ): Change {
        val pack =
            StickerPack(
                identifier = PackNaming.identifier(series, number),
                name = PackNaming.name(series, number),
                publisher = PackNaming.PUBLISHER,
                series = series,
                number = number,
                imageDataVersion = 1,
                stickers = stickers,
            )
        val stickerFile = StagedFile(newFile, pack.identifier, stickers.last().fileName)
        return Change(listOf(pack), listOf(stickerFile, trayFor(pack, firstSource)))
    }

    private suspend fun trayFor(
        pack: StickerPack,
        source: FileSource,
    ): StagedFile =
        StagedFile(FileSource.Temp(trayIcons.render(source).path), pack.identifier, StickerPack.TRAY_ICON_FILE)

    private suspend fun commit(
        packs: List<StickerPack>,
        change: Change,
    ) {
        val changedIds = change.changed.map { it.identifier }.toSet()
        val newState = packs.filterNot { it.identifier in changedIds } + change.changed
        repository.commit(newState.sortedWith(compareBy({ it.series }, { it.number })), change.staged, change.obsolete)
    }

    private companion object {
        const val MOVED_ON_SPLIT = 2
    }
}
