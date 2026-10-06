package com.edrl.stickerbridge.ui.thumbnails

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException

/**
 * Loads small static thumbnails (first frame) for the grid: remote ones with the same TikTok
 * referer as the converter, local ones from the pack files. Decoded at about [TARGET_SIZE] pixels
 * and kept in a memory cache, off the main thread (NFR3).
 */
class ThumbnailLoader(
    private val client: OkHttpClient,
) {
    private val cache = LruCache<String, ImageBitmap>(CACHE_ENTRIES)

    suspend fun remote(url: String): ImageBitmap? =
        cache.get(url) ?: withContext(Dispatchers.IO) {
            val bytes =
                try {
                    val request =
                        Request
                            .Builder()
                            .url(url)
                            .header("Referer", TIKTOK_REFERER)
                            .build()
                    client
                        .newCall(
                            request,
                        ).execute()
                        .use { if (it.isSuccessful) it.body?.bytesAtMost(MAX_BYTES) else null }
                } catch (_: IOException) {
                    null
                }
            bytes?.let { decode(it) }?.also { cache.put(url, it) }
        }

    suspend fun local(file: File): ImageBitmap? {
        val key = "${file.path}@${file.lastModified()}"
        return cache.get(key) ?: withContext(Dispatchers.IO) {
            if (file.exists()) decode(file.readBytes())?.also { cache.put(key, it) } else null
        }
    }

    /** The whole body, or null when it is larger than [limit] bytes: a thumbnail never is. */
    private fun ResponseBody.bytesAtMost(limit: Long): ByteArray? {
        val source = source()
        source.request(limit + 1)
        return if (source.buffer.size > limit) null else source.buffer.readByteArray()
    }

    private fun decode(bytes: ByteArray): ImageBitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= TARGET_SIZE &&
            bounds.outHeight / (sample * 2) >= TARGET_SIZE
        ) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.let(Bitmap::asImageBitmap)
    }

    private companion object {
        const val TARGET_SIZE = 256
        const val CACHE_ENTRIES = 120
        const val MAX_BYTES = 10L * 1024 * 1024
        const val TIKTOK_REFERER = "https://www.tiktok.com/"
    }
}

/** A thumbnail that appears when loaded; [placeholder] shows meanwhile or on failure. */
@Composable
fun Thumbnail(
    key: Any,
    load: suspend () -> ImageBitmap?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {},
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key) { value = load() }
    val loaded = bitmap
    if (loaded == null) {
        Box(modifier) { placeholder() }
    } else {
        Image(loaded, contentDescription, modifier, contentScale = ContentScale.Fit)
    }
}
