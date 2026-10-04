package com.edrl.stickerbridge.conversion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import com.aureusapps.android.webpandroid.CodecException
import com.aureusapps.android.webpandroid.encoder.WebPAnimEncoder
import com.aureusapps.android.webpandroid.encoder.WebPAnimEncoderOptions
import com.aureusapps.android.webpandroid.encoder.WebPConfig
import com.aureusapps.android.webpandroid.encoder.WebPMuxAnimParams
import com.aureusapps.android.webpandroid.encoder.WebPPreset
import com.edrl.stickerbridge.core.conversion.DecodedImage
import com.edrl.stickerbridge.core.conversion.EncodedFile
import com.edrl.stickerbridge.core.conversion.FitCalculator
import com.edrl.stickerbridge.core.conversion.Placement
import com.edrl.stickerbridge.core.conversion.SampledFrame
import com.edrl.stickerbridge.core.conversion.StickerEncoder
import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Draws images on a transparent 512×512 canvas and encodes them as lossy WebP files in [workDir]:
 * still stickers with Android's encoder, animated ones with libwebp (ADR-003).
 */
class WebpStickerEncoder(
    private val context: Context,
    private val workDir: File,
    private val log: DiagnosticLog,
) : StickerEncoder {
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    override suspend fun encodeStatic(
        image: DecodedImage,
        placement: Placement,
        quality: Int,
    ): EncodedFile =
        withContext(Dispatchers.Default) {
            val canvasBitmap = newCanvasBitmap()
            try {
                draw((image as AndroidDecodedImage).frame(0), placement, canvasBitmap)
                val file = newWorkFile()
                file.outputStream().use { canvasBitmap.compress(lossyWebp(), quality, it) }
                EncodedFile(file.absolutePath, file.length())
            } finally {
                canvasBitmap.recycle()
            }
        }

    override suspend fun encodeAnimated(
        image: DecodedImage,
        placement: Placement,
        frames: List<SampledFrame>,
        quality: Int,
    ): EncodedFile =
        withContext(Dispatchers.Default) {
            val source = image as AndroidDecodedImage
            val file = newWorkFile()
            val canvasBitmap = newCanvasBitmap()
            val encoder = newAnimEncoder(quality)
            try {
                var timestamp = 0L
                frames.forEach { frame ->
                    draw(source.frame(frame.sourceIndex), placement, canvasBitmap)
                    encoder.addFrame(timestamp, canvasBitmap)
                    timestamp += frame.durationMs
                }
                encoder.assemble(timestamp, Uri.fromFile(file))
                EncodedFile(file.absolutePath, file.length())
            } catch (e: CodecException) {
                // A failed attempt counts as too large, so the converter moves on to the next one.
                log.event(TAG, "animated encoding failed at quality $quality", e)
                file.delete()
                EncodedFile(file.absolutePath, Long.MAX_VALUE)
            } finally {
                encoder.release()
                canvasBitmap.recycle()
            }
        }

    override fun discard(file: EncodedFile) {
        File(file.path).delete()
    }

    private fun newAnimEncoder(quality: Int): WebPAnimEncoder =
        WebPAnimEncoder(
            context,
            FitCalculator.CANVAS,
            FitCalculator.CANVAS,
            WebPAnimEncoderOptions(
                minimizeSize = false,
                animParams = WebPMuxAnimParams(backgroundColor = Color.TRANSPARENT, loopCount = 0),
            ),
        ).configure(
            WebPConfig(
                lossless = WebPConfig.COMPRESSION_LOSSY,
                quality = quality.toFloat(),
                method = COMPRESSION_METHOD,
            ),
            WebPPreset.WEBP_PRESET_PICTURE,
        )

    private fun draw(
        frame: Bitmap,
        placement: Placement,
        target: Bitmap,
    ) {
        val canvas = Canvas(target)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val destination =
            Rect(
                placement.offsetX,
                placement.offsetY,
                placement.offsetX + placement.width,
                placement.offsetY + placement.height,
            )
        canvas.drawBitmap(frame, null, destination, paint)
    }

    private fun newCanvasBitmap(): Bitmap =
        Bitmap.createBitmap(FitCalculator.CANVAS, FitCalculator.CANVAS, Bitmap.Config.ARGB_8888)

    private fun newWorkFile(): File {
        workDir.mkdirs()
        return File(workDir, "${UUID.randomUUID()}.webp")
    }

    @Suppress("DEPRECATION")
    private fun lossyWebp(): Bitmap.CompressFormat =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            // Before Android 11 plain WEBP is lossy for any quality below 100.
            Bitmap.CompressFormat.WEBP
        }

    private companion object {
        const val TAG = "conversion"

        /** libwebp effort from 0 (fast) to 6 (small); 4 balances both for NFR2. */
        const val COMPRESSION_METHOD = 4
    }
}
