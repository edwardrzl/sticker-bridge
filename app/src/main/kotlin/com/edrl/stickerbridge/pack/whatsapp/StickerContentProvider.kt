package com.edrl.stickerbridge.pack.whatsapp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.edrl.stickerbridge.StickerBridgeApp
import com.edrl.stickerbridge.core.pack.PackSeries
import com.edrl.stickerbridge.core.pack.PackValidator
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StorageException
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException

/**
 * Serves this app's packs to WhatsApp with the contract WhatsApp defines. Readable only by
 * WhatsApp (manifest `readPermission`). Only packs that pass validation are exposed (BR-19).
 */
class StickerContentProvider : ContentProvider() {
    private val matcher =
        UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(WhatsAppContract.AUTHORITY, WhatsAppContract.METADATA, ALL_METADATA)
            addURI(WhatsAppContract.AUTHORITY, "${WhatsAppContract.METADATA}/*", ONE_METADATA)
            addURI(WhatsAppContract.AUTHORITY, "${WhatsAppContract.STICKERS}/*", STICKERS)
            addURI(WhatsAppContract.AUTHORITY, "${WhatsAppContract.STICKERS_ASSET}/*/*", ASSET)
        }

    private val repository by lazy {
        (requireNotNull(context).applicationContext as StickerBridgeApp).container.packRepository
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor =
        when (matcher.match(uri)) {
            ALL_METADATA -> metadataCursor(validPacks())
            ONE_METADATA -> metadataCursor(validPacks().filter { it.identifier == uri.lastPathSegment })
            STICKERS -> stickersCursor(validPacks().firstOrNull { it.identifier == uri.lastPathSegment })
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

    override fun openAssetFile(
        uri: Uri,
        mode: String,
    ): AssetFileDescriptor {
        val segments = uri.pathSegments
        if (matcher.match(uri) != ASSET || segments.size != ASSET_SEGMENTS) {
            throw FileNotFoundException("Unknown URI: $uri")
        }
        val (_, identifier, fileName) = segments
        val pack =
            validPacks().firstOrNull { it.identifier == identifier }
                ?: throw FileNotFoundException("Unknown pack: $identifier")
        val known = fileName == StickerPack.TRAY_ICON_FILE || pack.stickers.any { it.fileName == fileName }
        val file =
            repository.fileOf(identifier, fileName)?.takeIf { known && it.exists() }
                ?: throw FileNotFoundException("Unknown file: $fileName")
        return AssetFileDescriptor(
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY),
            0,
            AssetFileDescriptor.UNKNOWN_LENGTH,
        )
    }

    override fun getType(uri: Uri): String? =
        when (matcher.match(uri)) {
            ALL_METADATA -> "vnd.android.cursor.dir/vnd.${WhatsAppContract.AUTHORITY}.${WhatsAppContract.METADATA}"
            ONE_METADATA -> "vnd.android.cursor.item/vnd.${WhatsAppContract.AUTHORITY}.${WhatsAppContract.METADATA}"
            STICKERS -> "vnd.android.cursor.dir/vnd.${WhatsAppContract.AUTHORITY}.${WhatsAppContract.STICKERS}"
            ASSET -> if (uri.lastPathSegment == StickerPack.TRAY_ICON_FILE) "image/png" else "image/webp"
            else -> null
        }

    private fun validPacks(): List<StickerPack> =
        try {
            runBlocking { repository.load() }.filter { PackValidator.validate(it).isEmpty() }
        } catch (_: StorageException) {
            emptyList()
        }

    private fun metadataCursor(packs: List<StickerPack>): Cursor =
        MatrixCursor(METADATA_COLUMNS).apply {
            packs.forEach { pack ->
                addRow(
                    arrayOf<Any?>(
                        pack.identifier,
                        pack.name,
                        pack.publisher,
                        StickerPack.TRAY_ICON_FILE,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        pack.imageDataVersion.toString(),
                        0,
                        if (pack.series == PackSeries.ANIMATED) 1 else 0,
                    ),
                )
            }
        }

    private fun stickersCursor(pack: StickerPack?): Cursor =
        MatrixCursor(STICKER_COLUMNS).apply {
            pack?.stickers?.forEach { addRow(arrayOf<Any?>(it.fileName, it.emoji, "")) }
        }

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri = throw UnsupportedOperationException("read-only provider")

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = throw UnsupportedOperationException("read-only provider")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = throw UnsupportedOperationException("read-only provider")

    private companion object {
        const val ALL_METADATA = 1
        const val ONE_METADATA = 2
        const val STICKERS = 3
        const val ASSET = 4
        const val ASSET_SEGMENTS = 3

        val METADATA_COLUMNS =
            arrayOf(
                WhatsAppContract.PACK_IDENTIFIER,
                WhatsAppContract.PACK_NAME,
                WhatsAppContract.PACK_PUBLISHER,
                WhatsAppContract.PACK_ICON,
                WhatsAppContract.ANDROID_STORE_LINK,
                WhatsAppContract.IOS_STORE_LINK,
                WhatsAppContract.PUBLISHER_EMAIL,
                WhatsAppContract.PUBLISHER_WEBSITE,
                WhatsAppContract.PRIVACY_POLICY,
                WhatsAppContract.LICENSE_AGREEMENT,
                WhatsAppContract.IMAGE_DATA_VERSION,
                WhatsAppContract.AVOID_CACHE,
                WhatsAppContract.ANIMATED_PACK,
            )

        val STICKER_COLUMNS =
            arrayOf(
                WhatsAppContract.STICKER_FILE_NAME,
                WhatsAppContract.STICKER_EMOJI,
                WhatsAppContract.STICKER_ACCESSIBILITY_TEXT,
            )
    }
}
