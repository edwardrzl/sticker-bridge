package com.edrl.stickerbridge.di

import android.content.Context
import com.edrl.stickerbridge.conversion.BitmapImageSource
import com.edrl.stickerbridge.conversion.PngTrayIconRenderer
import com.edrl.stickerbridge.conversion.WebpStickerEncoder
import com.edrl.stickerbridge.core.conversion.StickerConverter
import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import com.edrl.stickerbridge.core.extraction.CommentImageExtractor
import com.edrl.stickerbridge.core.pack.PackService
import com.edrl.stickerbridge.core.pack.StickerPackPublisher
import com.edrl.stickerbridge.extraction.WebViewCommentImageExtractor
import com.edrl.stickerbridge.pack.storage.FilePackRepository
import com.edrl.stickerbridge.pack.whatsapp.WhatsAppPublisher
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/** Builds the app's object graph by hand (no DI framework): adapters from `app`, rules from `core`. */
class AppContainer(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val workDir = File(appContext.cacheDir, "conversion")

    val diagnostics: DiagnosticLog = AndroidDiagnosticLog()

    private val httpClient: OkHttpClient =
        OkHttpClient
            .Builder()
            .callTimeout(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

    val extractor: CommentImageExtractor = WebViewCommentImageExtractor(appContext, diagnostics)

    private val encoder = WebpStickerEncoder(workDir)

    val converter = StickerConverter(BitmapImageSource(appContext, httpClient, diagnostics), encoder)

    val packRepository = FilePackRepository(appContext.filesDir)

    val packService = PackService(packRepository, PngTrayIconRenderer(workDir))

    val publisher: StickerPackPublisher = WhatsAppPublisher(appContext, diagnostics)

    private companion object {
        const val DOWNLOAD_TIMEOUT_SECONDS = 15L
    }
}
