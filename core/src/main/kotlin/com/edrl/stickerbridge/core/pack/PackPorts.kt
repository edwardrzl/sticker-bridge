package com.edrl.stickerbridge.core.pack

import com.edrl.stickerbridge.core.conversion.EncodedFile

/** A file to move into a pack folder as part of a commit. */
data class StagedFile(
    val sourcePath: String,
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
 * [commit] is atomic (BR-21): it moves the staged files into place and then replaces the whole
 * pack index at once. If it throws [StorageException], the previously stored packs are intact.
 */
interface PackRepository {
    suspend fun load(): List<StickerPack>

    suspend fun commit(
        packs: List<StickerPack>,
        staged: List<StagedFile>,
    )
}

/** Port: talks to WhatsApp about the packs this app provides. */
interface StickerPackPublisher {
    fun isWhatsAppInstalled(): Boolean

    /** Asks WhatsApp whether the pack is added; false when WhatsApp cannot tell (ADR-006). */
    suspend fun isAdded(identifier: String): Boolean

    /** Tells WhatsApp a pack's content changed so it reloads it. */
    fun notifyChanged(pack: StickerPack)
}

/** Port: renders the 96×96 tray icon of a pack from one of its stickers. */
fun interface TrayIconRenderer {
    suspend fun render(stickerFile: EncodedFile): EncodedFile
}

fun interface StickerIdGenerator {
    fun next(): String
}
