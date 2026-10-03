package com.edrl.stickerbridge.core.pack

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Reads and writes the pack index file (`packs.json`, ADR-004). */
object PackIndexCodec {
    const val SCHEMA_VERSION = 1

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    fun encode(packs: List<StickerPack>): String = json.encodeToString(IndexDto(SCHEMA_VERSION, packs.map(::toDto)))

    fun decode(text: String): List<StickerPack> {
        val index =
            try {
                json.decodeFromString<IndexDto>(text)
            } catch (e: IllegalArgumentException) {
                // Also covers SerializationException, which extends it.
                throw StorageException("pack index is corrupted", e)
            }
        if (index.schemaVersion != SCHEMA_VERSION) {
            throw StorageException("unsupported pack index schema ${index.schemaVersion}")
        }
        return index.packs.map(::fromDto)
    }

    private fun toDto(pack: StickerPack) =
        PackDto(
            identifier = pack.identifier,
            name = pack.name,
            publisher = pack.publisher,
            series = pack.series,
            number = pack.number,
            imageDataVersion = pack.imageDataVersion,
            stickers = pack.stickers.map { StickerDto(it.id, it.fileName, it.emoji, it.sizeBytes) },
        )

    private fun fromDto(dto: PackDto) =
        StickerPack(
            identifier = dto.identifier,
            name = dto.name,
            publisher = dto.publisher,
            series = dto.series,
            number = dto.number,
            imageDataVersion = dto.imageDataVersion,
            stickers = dto.stickers.map { Sticker(it.id, it.fileName, it.emoji, it.sizeBytes) },
        )

    @Serializable
    private data class IndexDto(
        val schemaVersion: Int,
        val packs: List<PackDto>,
    )

    @Serializable
    private data class PackDto(
        val identifier: String,
        val name: String,
        val publisher: String,
        val series: PackSeries,
        val number: Int,
        val imageDataVersion: Int,
        val stickers: List<StickerDto>,
    )

    @Serializable
    private data class StickerDto(
        val id: String,
        val fileName: String,
        val emoji: String,
        val sizeBytes: Long,
    )
}
