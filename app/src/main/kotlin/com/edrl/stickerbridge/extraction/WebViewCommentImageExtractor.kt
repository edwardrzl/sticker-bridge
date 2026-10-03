package com.edrl.stickerbridge.extraction

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import com.edrl.stickerbridge.core.extraction.CommentImage
import com.edrl.stickerbridge.core.extraction.CommentImageExtractor
import com.edrl.stickerbridge.core.extraction.CommentResponseParser
import com.edrl.stickerbridge.core.extraction.ExtractionError
import com.edrl.stickerbridge.core.extraction.ExtractionOutcome
import com.edrl.stickerbridge.core.extraction.ExtractionPage
import com.edrl.stickerbridge.core.extraction.ExtractionSession
import com.edrl.stickerbridge.core.extraction.HostAllowlist
import com.edrl.stickerbridge.core.extraction.ParsedComments
import com.edrl.stickerbridge.core.link.PostLink
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Extracts comment images by loading the public post page in a hidden, non-interactive WebView
 * (ADR-002). A script injected at document start forwards the comment-list responses the page
 * requests; the app never signs or sends TikTok API requests itself.
 *
 * Sessions must be used from the main thread, as WebView requires.
 */
class WebViewCommentImageExtractor(
    private val context: Context,
    private val log: DiagnosticLog,
    private val allowlist: HostAllowlist = HostAllowlist.TIKTOK,
    private val parser: CommentResponseParser = CommentResponseParser(),
) : CommentImageExtractor {
    override fun open(link: PostLink): ExtractionSession = Session(link)

    private sealed interface Event {
        data class Body(
            val text: String,
        ) : Event

        data class Failure(
            val error: ExtractionError,
        ) : Event
    }

    private inner class Session(
        private val link: PostLink,
    ) : ExtractionSession {
        private val events = Channel<Event>(Channel.UNLIMITED)
        private val blockedHosts = ConcurrentHashMap.newKeySet<String>()
        private var webView: WebView? = null

        override suspend fun loadInitial(): ExtractionOutcome {
            if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT) ||
                !WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)
            ) {
                log.event(TAG, "WebView lacks document-start scripts or message listeners")
                return ExtractionOutcome.Failed(ExtractionError.UnexpectedFormat("unsupported WebView"))
            }
            createWebView().loadUrl(link.url)
            return awaitFirstImages()
        }

        /** Loading more comments arrives with unit U2. */
        override suspend fun loadMore(): ExtractionOutcome =
            ExtractionOutcome.Failed(ExtractionError.UnexpectedFormat("load more is not available yet"))

        override fun close() {
            webView?.let { view ->
                view.stopLoading()
                view.clearCache(true)
                view.destroy()
            }
            webView = null
            CookieManager.getInstance().removeAllCookies(null)
            WebStorage.getInstance().deleteAllData()
            events.close()
        }

        /** The walking skeleton stops at the first response with images (or at 30 s, BR-04). */
        private suspend fun awaitFirstImages(): ExtractionOutcome {
            val images = LinkedHashMap<String, CommentImage>()
            var lastMalformed: String? = null
            var hasMore = false
            val outcome =
                withTimeoutOrNull(FIRST_BATCH_TIMEOUT_MS) {
                    for (event in events) {
                        when (event) {
                            is Event.Failure -> return@withTimeoutOrNull ExtractionOutcome.Failed(event.error)
                            is Event.Body ->
                                when (val parsed = parser.parse(event.text)) {
                                    is ParsedComments.Malformed -> lastMalformed = parsed.detail
                                    is ParsedComments.Parsed -> {
                                        parsed.images.forEach { images.putIfAbsent(it.url, it) }
                                        hasMore = parsed.hasMore
                                        log.event(TAG, "comment batch: ${parsed.images.size} images")
                                    }
                                }
                        }
                        if (images.isNotEmpty()) break
                    }
                    ExtractionOutcome.Loaded(ExtractionPage(images.values.toList(), hasMore))
                }
            log.event(TAG, "blocked hosts: ${blockedHosts.sorted()}")
            return outcome ?: timeoutOutcome(lastMalformed)
        }

        private fun timeoutOutcome(lastMalformed: String?): ExtractionOutcome {
            val error = lastMalformed?.let(ExtractionError::UnexpectedFormat) ?: ExtractionError.Timeout
            log.event(TAG, "no images within ${FIRST_BATCH_TIMEOUT_MS}ms: $error")
            return ExtractionOutcome.Failed(error)
        }

        @SuppressLint("SetJavaScriptEnabled")
        private fun createWebView(): WebView {
            val view = WebView(context)
            view.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                userAgentString = DESKTOP_USER_AGENT
            }
            WebViewCompat.addWebMessageListener(view, CHANNEL_NAME, ALLOWED_ORIGINS) { _, message, _, _, _ ->
                message.data?.let { events.trySend(Event.Body(it)) }
            }
            WebViewCompat.addDocumentStartJavaScript(view, captureScript(), ALLOWED_ORIGINS)
            view.webViewClient = Client()
            webView = view
            return view
        }

        private fun captureScript(): String =
            context.assets
                .open(SCRIPT_ASSET)
                .bufferedReader()
                .use { it.readText() }

        private inner class Client : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest,
            ): WebResourceResponse? {
                val host = request.url.host.orEmpty()
                if (allowlist.isAllowed(host)) return null
                blockedHosts += host
                return WebResourceResponse("text/plain", "utf-8", HTTP_FORBIDDEN, "Blocked", emptyMap(), emptyBody())
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val path = request.url.path.orEmpty()
                if (request.isForMainFrame && LOGIN_PATHS.any { path.startsWith(it) }) {
                    events.trySend(Event.Failure(ExtractionError.CommentsBlocked))
                    return true
                }
                return false
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                if (!request.isForMainFrame) return
                log.event(TAG, "main frame error ${error.errorCode}")
                events.trySend(Event.Failure(networkError(error.errorCode)))
            }

            /** A crashed or killed browser renderer must end the extraction, never the app (FR6.4). */
            override fun onRenderProcessGone(
                view: WebView,
                detail: RenderProcessGoneDetail,
            ): Boolean {
                log.event(TAG, "browser renderer gone, crashed=${detail.didCrash()}")
                if (webView === view) webView = null
                view.destroy()
                events.trySend(Event.Failure(ExtractionError.UnexpectedFormat("browser renderer stopped")))
                return true
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse,
            ) {
                if (!request.isForMainFrame) return
                log.event(TAG, "main frame HTTP ${errorResponse.statusCode}")
                events.trySend(Event.Failure(ExtractionError.PostUnavailable))
            }
        }
    }

    private companion object {
        const val TAG = "extraction"
        const val CHANNEL_NAME = "StickerBridge"
        const val SCRIPT_ASSET = "comment-capture.js"
        const val FIRST_BATCH_TIMEOUT_MS = 30_000L
        const val HTTP_FORBIDDEN = 403
        val ALLOWED_ORIGINS = setOf("https://www.tiktok.com")
        val LOGIN_PATHS = listOf("/login", "/signup")

        /** A desktop browser identity, so the post page shows its comments without opening a panel. */
        const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/140.0.0.0 Safari/537.36"

        fun emptyBody() = ByteArrayInputStream(ByteArray(0))

        fun networkError(code: Int): ExtractionError =
            when (code) {
                WebViewClient.ERROR_HOST_LOOKUP,
                WebViewClient.ERROR_CONNECT,
                WebViewClient.ERROR_TIMEOUT,
                -> ExtractionError.NoConnection
                else -> ExtractionError.PostUnavailable
            }
    }
}
