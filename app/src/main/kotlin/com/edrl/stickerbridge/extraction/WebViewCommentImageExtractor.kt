package com.edrl.stickerbridge.extraction

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.view.View
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
import com.edrl.stickerbridge.core.extraction.CommentImageOrder
import com.edrl.stickerbridge.core.extraction.CommentResponseParser
import com.edrl.stickerbridge.core.extraction.ExtractionError
import com.edrl.stickerbridge.core.extraction.ExtractionOutcome
import com.edrl.stickerbridge.core.extraction.ExtractionPage
import com.edrl.stickerbridge.core.extraction.ExtractionSession
import com.edrl.stickerbridge.core.extraction.HostAllowlist
import com.edrl.stickerbridge.core.extraction.InitialLoadPolicy
import com.edrl.stickerbridge.core.extraction.LoadDecision
import com.edrl.stickerbridge.core.extraction.ParsedComments
import com.edrl.stickerbridge.core.link.PostLink
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.TimeSource

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

    /** What one session has received so far. */
    private class Collected {
        val images = mutableListOf<CommentImage>()
        var batches = 0
        var hasMore = true
        var lastMalformed: String? = null
        var failure: ExtractionError? = null
    }

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
        private val collected = Collected()
        private var webView: WebView? = null

        override suspend fun loadInitial(): ExtractionOutcome {
            if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT) ||
                !WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)
            ) {
                log.event(TAG, "WebView lacks document-start scripts or message listeners")
                return ExtractionOutcome.Failed(ExtractionError.UnexpectedFormat("unsupported WebView"))
            }
            createWebView().loadUrl(link.url)
            return awaitInitialLoad()
        }

        /** Asks the page for one more batch of comments (FR2.4); nothing is requested when TikTok has no more. */
        override suspend fun loadMore(): ExtractionOutcome {
            val view = webView
            if (view == null || !collected.hasMore) return loadedOutcome()
            val before = collected.batches
            collected.failure = null
            view.evaluateJavascript(LOAD_MORE_CALL, null)
            withTimeoutOrNull(LOAD_MORE_TIMEOUT_MS) {
                var receiving = true
                while (collected.batches == before && receiving) receiving = receiveInto(collected)
            }
            val failure = collected.failure
            return when {
                collected.batches > before -> loadedOutcome()
                failure != null -> ExtractionOutcome.Failed(failure)
                else -> ExtractionOutcome.Failed(ExtractionError.Timeout)
            }
        }

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

        /** Collects batches until [InitialLoadPolicy] says the initial load is over. */
        private suspend fun awaitInitialLoad(): ExtractionOutcome {
            val start = TimeSource.Monotonic.markNow()
            var firstBatch: TimeSource.Monotonic.ValueTimeMark? = null
            var decision = LoadDecision.Wait
            var receiving = true
            while (decision == LoadDecision.Wait && receiving) {
                val sinceFirst = firstBatch?.elapsedNow()?.inWholeMilliseconds
                val remaining =
                    InitialLoadPolicy.remainingMs(collected.batches, start.elapsedNow().inWholeMilliseconds, sinceFirst)
                receiving = withTimeoutOrNull(remaining) { receiveInto(collected) } ?: true
                if (collected.batches > 0 && firstBatch == null) firstBatch = TimeSource.Monotonic.markNow()
                decision =
                    InitialLoadPolicy.decide(
                        collected.batches,
                        collected.hasMore,
                        start.elapsedNow().inWholeMilliseconds,
                        firstBatch?.elapsedNow()?.inWholeMilliseconds,
                    )
            }
            log.event(
                TAG,
                "batches ${collected.batches}, images ${collected.images.size}, blocked ${blockedHosts.sorted()}",
            )
            val failure = collected.failure
            return when {
                collected.batches > 0 -> loadedOutcome()
                failure != null -> ExtractionOutcome.Failed(failure)
                else ->
                    ExtractionOutcome.Failed(
                        collected.lastMalformed?.let(ExtractionError::UnexpectedFormat) ?: ExtractionError.Timeout,
                    )
            }
        }

        private fun loadedOutcome(): ExtractionOutcome =
            ExtractionOutcome.Loaded(ExtractionPage(CommentImageOrder.byLikes(collected.images), collected.hasMore))

        /** Handles one event; false when no more batches will come (failure or closed session). */
        private suspend fun receiveInto(collected: Collected): Boolean {
            val event = events.receiveCatching().getOrNull() ?: return false
            when (event) {
                is Event.Failure -> collected.failure = event.error
                is Event.Body ->
                    when (val parsed = parser.parse(event.text)) {
                        is ParsedComments.Malformed -> collected.lastMalformed = parsed.detail
                        is ParsedComments.Parsed -> {
                            collected.images += parsed.images
                            collected.hasMore = parsed.hasMore
                            collected.batches++
                            log.event(TAG, "comment batch ${collected.batches}: ${parsed.images.size} images")
                        }
                    }
            }
            return collected.failure == null
        }

        @SuppressLint("SetJavaScriptEnabled")
        private fun createWebView(): WebView {
            // Debug builds can be inspected from desktop Chrome (chrome://inspect) to diagnose TikTok changes.
            WebView.setWebContentsDebuggingEnabled(
                context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
            )
            val view = WebView(context)
            view.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                userAgentString = DESKTOP_USER_AGENT
            }
            WebViewCompat.addWebMessageListener(view, CHANNEL_NAME, ALLOWED_ORIGINS) { _, message, _, _, _ ->
                message.data?.let(::onScriptMessage)
            }
            WebViewCompat.addDocumentStartJavaScript(view, captureScript(), ALLOWED_ORIGINS)
            view.webViewClient = Client()
            // A desktop-size viewport, so the page behaves as in a desktop browser even though the
            // view is never shown.
            view.measure(
                View.MeasureSpec.makeMeasureSpec(VIEWPORT_WIDTH, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(VIEWPORT_HEIGHT, View.MeasureSpec.EXACTLY),
            )
            view.layout(0, 0, VIEWPORT_WIDTH, VIEWPORT_HEIGHT)
            webView = view
            return view
        }

        private fun onScriptMessage(data: String) {
            when {
                data.startsWith(BODY_PREFIX) -> events.trySend(Event.Body(data.removePrefix(BODY_PREFIX)))
                data.startsWith(DIAGNOSTIC_PREFIX) -> log.event(TAG, "page: ${data.removePrefix(DIAGNOSTIC_PREFIX)}")
                data.startsWith(FAILURE_PREFIX) -> {
                    val reason = data.removePrefix(FAILURE_PREFIX)
                    log.event(TAG, "comment request gave up: $reason")
                    events.trySend(Event.Failure(ExtractionError.UnexpectedFormat(reason)))
                }
            }
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

            override fun onPageStarted(
                view: WebView,
                url: String,
                favicon: Bitmap?,
            ) {
                log.event(TAG, "page started: ${hostAndPath(url)}")
            }

            override fun onPageFinished(
                view: WebView,
                url: String,
            ) {
                log.event(TAG, "page finished: ${hostAndPath(url)}")
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
        const val LOAD_MORE_TIMEOUT_MS = 15_000L
        const val LOAD_MORE_CALL = "window.__stickerBridgeLoadMore && window.__stickerBridgeLoadMore()"
        const val FAILURE_PREFIX = "fail:"
        const val HTTP_FORBIDDEN = 403
        const val VIEWPORT_WIDTH = 1280
        const val VIEWPORT_HEIGHT = 2400
        const val BODY_PREFIX = "body:"
        const val DIAGNOSTIC_PREFIX = "diag:"

        /** Only host and path reach the log: query strings may carry identifiers. */
        fun hostAndPath(url: String): String {
            val uri = android.net.Uri.parse(url)
            return "${uri.host}${uri.path}"
        }

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
