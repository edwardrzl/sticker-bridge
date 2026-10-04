package com.edrl.stickerbridge.conversion

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.edrl.stickerbridge.core.conversion.EncodedFile
import com.edrl.stickerbridge.core.pack.FileSource
import com.edrl.stickerbridge.core.pack.StickerLimits
import com.edrl.stickerbridge.core.pack.StorageException
import com.edrl.stickerbridge.core.pack.TrayIconRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Renders a pack's 96×96 PNG tray icon from one of its sticker files (first frame if animated). */
class PngTrayIconRenderer(
    private val workDir: File,
    private val resolve: (FileSource) -> File?,
) : TrayIconRenderer {
    override suspend fun render(sticker: FileSource): EncodedFile =
        withContext(Dispatchers.Default) {
            val source = resolve(sticker) ?: throw StorageException("unknown sticker file $sticker")
            val decoded =
                BitmapFactory.decodeFile(source.path)
                    ?: throw StorageException("cannot decode sticker for tray icon")
            val icon =
                Bitmap.createScaledBitmap(decoded, StickerLimits.TRAY_ICON_SIZE, StickerLimits.TRAY_ICON_SIZE, true)
            try {
                workDir.mkdirs()
                val file = File(workDir, "${UUID.randomUUID()}.png")
                file.outputStream().use { icon.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY_IGNORED, it) }
                EncodedFile(file.absolutePath, file.length())
            } finally {
                if (icon !== decoded) icon.recycle()
                decoded.recycle()
            }
        }

    private companion object {
        /** PNG is lossless; Android ignores the quality argument for it. */
        const val PNG_QUALITY_IGNORED = 100
    }
}
