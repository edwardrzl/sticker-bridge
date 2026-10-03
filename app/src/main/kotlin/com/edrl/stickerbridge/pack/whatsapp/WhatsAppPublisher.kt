package com.edrl.stickerbridge.pack.whatsapp

import android.content.Context
import android.content.pm.PackageManager
import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import com.edrl.stickerbridge.core.pack.StickerPack
import com.edrl.stickerbridge.core.pack.StickerPackPublisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Asks WhatsApp about this app's packs instead of remembering it (ADR-006). */
class WhatsAppPublisher(
    private val context: Context,
    private val log: DiagnosticLog,
) : StickerPackPublisher {
    override fun isWhatsAppInstalled(): Boolean =
        try {
            context.packageManager.getPackageInfo(WhatsAppContract.PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }

    override suspend fun isAdded(identifier: String): Boolean =
        withContext(Dispatchers.IO) {
            if (!isWhatsAppInstalled()) return@withContext false
            try {
                context.contentResolver
                    .query(WhatsAppContract.whitelistQuery(identifier), null, null, null, null)
                    ?.use { cursor ->
                        cursor.moveToFirst() &&
                            cursor.getInt(cursor.getColumnIndexOrThrow(WhatsAppContract.WHITELIST_RESULT)) == 1
                    } ?: false
            } catch (e: SecurityException) {
                log.event(TAG, "WhatsApp refused the added-pack query", e)
                false
            } catch (e: IllegalArgumentException) {
                log.event(TAG, "WhatsApp answered the added-pack query in an unknown format", e)
                false
            }
        }

    override fun notifyChanged(pack: StickerPack) {
        context.contentResolver.notifyChange(WhatsAppContract.packMetadataUri(pack.identifier), null)
        log.event(TAG, "pack ${pack.identifier} changed to version ${pack.imageDataVersion}")
    }

    private companion object {
        const val TAG = "whatsapp"
    }
}
