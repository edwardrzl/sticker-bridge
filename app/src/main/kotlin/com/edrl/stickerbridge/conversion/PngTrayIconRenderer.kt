package com.edrl.stickerbridge.conversion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.edrl.stickerbridge.core.conversion.EncodedFile
import com.edrl.stickerbridge.core.pack.StickerLimits
import com.edrl.stickerbridge.core.pack.StorageException
import com.edrl.stickerbridge.core.pack.TrayIconRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Renders a pack's 96×96 PNG tray icon from one of its sticker files. */
class PngTrayIconRenderer(
    private val workDir: File,
) : TrayIconRenderer {
    override suspend fun render(stickerFile: EncodedFile): EncodedFile =
        withContext(Dispatchers.Default) {
            val sticker =
                BitmapFactory.decodeFile(stickerFile.path)
                    ?: throw StorageException("cannot decode sticker for tray icon")
            val icon =
                Bitmap.createScaledBitmap(
                    sticker,
                    StickerLimits.TRAY_ICON_SIZE,
                    StickerLimits.TRAY_ICON_SIZE,
                    true,
                )
            try {
                workDir.mkdirs()
                val file = File(workDir, "${UUID.randomUUID()}.png")
                file.outputStream().use { icon.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY_IGNORED, it) }
                EncodedFile(file.absolutePath, file.length())
            } finally {
                if (icon !== sticker) icon.recycle()
                sticker.recycle()
            }
        }

    private companion object {
        /** PNG is lossless; Android ignores the quality argument for it. */
        const val PNG_QUALITY_IGNORED = 100
    }
}
