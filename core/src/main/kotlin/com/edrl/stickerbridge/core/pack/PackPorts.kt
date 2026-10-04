package com.edrl.stickerbridge.core.pack

import com.edrl.stickerbridge.core.conversion.EncodedFile

/** Where a file to place in a pack comes from. */
sealed interface FileSource {
    /** A temporary file produced by the converter or the tray renderer. */
    data class Temp(
        val path: String,
    ) : FileSource

    /** A file already stored in a pack, copied when a full pack is split (BR-14). */
    data class InPack(
        val packIdentifier: String,
        val fileName: String,
    ) : FileSource
}

/** A file to place in a pack folder as part of a commit. */
data class StagedFile(
    val source: FileSource,
    val packIdentifier: String,
    val fileName: String,
)

/** A stored file that no pack references any more after a commit. */
data class PackFile(
    val packIdentifier: String,
    val fileName: String,
)

class StorageException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Port: stores packs and their files on the phone.
 *
 * [commit] is atomic (BR-21): it places the staged files, then replaces the whole pack index at
 * once, and only then deletes the [obsolete][commit] files. If it throws [StorageException]
 * before the index is replaced, the previously stored packs are intact.
 */
interface PackRepository {
    suspend fun load(): List<StickerPack>

    suspend fun commit(
        packs: List<StickerPack>,
        staged: List<StagedFile>,
        obsolete: List<PackFile> = emptyList(),
    )
}

/** Port: talks to WhatsApp about the packs this app provides. */
interface StickerPackPublisher {
    fun isWhatsAppInstalled(): Boolean

    /** Asks WhatsApp whether the pack is added; false when WhatsApp cannot tell (ADR-006). */
    suspend fun isAdded(identifier: String): Boolean

    /** Tells WhatsApp a pack's content changed. WhatsApp only reloads it from its add screen (FR5.6). */
    fun notifyChanged(pack: StickerPack)
}

/** Port: renders the 96×96 tray icon of a pack from one of its stickers. */
fun interface TrayIconRenderer {
    suspend fun render(sticker: FileSource): EncodedFile
}

fun interface StickerIdGenerator {
    fun next(): String
}
