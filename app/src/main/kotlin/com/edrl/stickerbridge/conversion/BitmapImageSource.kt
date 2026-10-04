package com.edrl.stickerbridge.conversion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.edrl.stickerbridge.core.conversion.ConversionError
import com.edrl.stickerbridge.core.conversion.DecodedImage
import com.edrl.stickerbridge.core.conversion.ImageOutcome
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.conversion.ImageSource
import com.edrl.stickerbridge.core.conversion.SourceImageInfo
import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/** A decoded bitmap. In the walking skeleton only the first frame of an image is decoded. */
class BitmapDecodedImage(
    val bitmap: Bitmap,
) : DecodedImage {
    override val info = SourceImageInfo(bitmap.width, bitmap.height, frameCount = 1, totalDurationMs = 0)

    override fun close() = bitmap.recycle()
}

/** Downloads remote images (with TikTok as referer) or reads gallery images, then decodes them. */
class BitmapImageSource(
    private val context: Context,
    private val client: OkHttpClient,
    private val log: DiagnosticLog,
) : ImageSource {
    override suspend fun open(ref: ImageRef): ImageOutcome =
        withContext(Dispatchers.IO) {
            val bytes =
                try {
                    when (ref) {
                        is ImageRef.Remote -> download(ref.url)
                        is ImageRef.Local -> readLocal(ref.uri)
                    }
                } catch (e: IOException) {
                    log.event(TAG, "could not read image", e)
                    null
                } ?: return@withContext ImageOutcome.Failed(ConversionError.DownloadFailed)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap == null) {
                ImageOutcome.Failed(ConversionError.UnsupportedImage)
            } else {
                ImageOutcome.Opened(BitmapDecodedImage(bitmap))
            }
        }

    private fun download(url: String): ByteArray? {
        val request =
            Request
                .Builder()
                .url(url)
                .header("Referer", TIKTOK_REFERER)
                .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                log.event(TAG, "download failed with HTTP ${response.code}")
                return null
            }
            return response.body.byteStream().readAtMost(MAX_IMAGE_BYTES)
        }
    }

    private fun readLocal(uri: String): ByteArray? =
        context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readAtMost(MAX_IMAGE_BYTES) }

    /** Reads the stream fully, or returns null when it is larger than [limit] bytes. */
    private fun InputStream.readAtMost(limit: Int): ByteArray? {
        val output = ByteArrayOutputStream()
        val chunk = ByteArray(CHUNK_BYTES)
        while (true) {
            val read = read(chunk)
            if (read < 0) break
            if (output.size() + read > limit) {
                log.event(TAG, "image larger than $limit bytes rejected")
                return null
            }
            output.write(chunk, 0, read)
        }
        return output.toByteArray()
    }

    private companion object {
        const val TAG = "conversion"
        const val TIKTOK_REFERER = "https://www.tiktok.com/"
        const val MAX_IMAGE_BYTES = 10 * 1024 * 1024
        const val CHUNK_BYTES = 64 * 1024
    }
}
