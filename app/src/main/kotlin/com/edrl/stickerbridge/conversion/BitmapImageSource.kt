package com.edrl.stickerbridge.conversion

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.edrl.stickerbridge.core.conversion.ConversionError
import com.edrl.stickerbridge.core.conversion.DecodedImage
import com.edrl.stickerbridge.core.conversion.ImageOutcome
import com.edrl.stickerbridge.core.conversion.ImageRef
import com.edrl.stickerbridge.core.conversion.ImageSource
import com.edrl.stickerbridge.core.conversion.WebpSniffer
import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Downloads remote images (with TikTok as referer) or reads gallery images, then decodes them:
 * animated WebP frame by frame, everything else as a still image.
 */
class BitmapImageSource(
    private val context: Context,
    private val client: OkHttpClient,
    private val log: DiagnosticLog,
    private val animatedDecoder: AnimatedWebpDecoder,
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

            val image: DecodedImage? =
                if (WebpSniffer.isAnimatedWebp(bytes)) {
                    animatedDecoder.decode(bytes)
                } else {
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let(::BitmapDecodedImage)
                }
            image?.let { ImageOutcome.Opened(it) } ?: ImageOutcome.Failed(ConversionError.UnsupportedImage)
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
            return response.body?.byteStream()?.readAtMost(MAX_IMAGE_BYTES)
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
