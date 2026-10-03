package com.edrl.stickerbridge.conversion

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import com.edrl.stickerbridge.core.conversion.DecodedImage
import com.edrl.stickerbridge.core.conversion.EncodedFile
import com.edrl.stickerbridge.core.conversion.FitCalculator
import com.edrl.stickerbridge.core.conversion.Placement
import com.edrl.stickerbridge.core.conversion.StickerEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Draws the image on a transparent 512×512 canvas and encodes it as a lossy WebP file in
 * [workDir]. Animated encoding with libwebp arrives in unit U3.
 */
class WebpStickerEncoder(
    private val workDir: File,
) : StickerEncoder {
    override suspend fun encodeStatic(
        image: DecodedImage,
        placement: Placement,
        quality: Int,
    ): EncodedFile =
        withContext(Dispatchers.Default) {
            val source = (image as BitmapDecodedImage).bitmap
            val canvasBitmap = Bitmap.createBitmap(FitCalculator.CANVAS, FitCalculator.CANVAS, Bitmap.Config.ARGB_8888)
            try {
                Canvas(canvasBitmap).drawBitmap(
                    source,
                    null,
                    Rect(
                        placement.offsetX,
                        placement.offsetY,
                        placement.offsetX + placement.width,
                        placement.offsetY + placement.height,
                    ),
                    Paint(Paint.FILTER_BITMAP_FLAG),
                )
                val file = newWorkFile("webp")
                file.outputStream().use { canvasBitmap.compress(lossyWebp(), quality, it) }
                EncodedFile(file.absolutePath, file.length())
            } finally {
                canvasBitmap.recycle()
            }
        }

    override fun discard(file: EncodedFile) {
        File(file.path).delete()
    }

    private fun newWorkFile(extension: String): File {
        workDir.mkdirs()
        return File(workDir, "${UUID.randomUUID()}.$extension")
    }

    @Suppress("DEPRECATION")
    private fun lossyWebp(): Bitmap.CompressFormat =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            // Before Android 11 plain WEBP is lossy for any quality below 100.
            Bitmap.CompressFormat.WEBP
        }
}
