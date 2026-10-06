package com.edrl.stickerbridge.net

import com.edrl.stickerbridge.core.diagnostics.DiagnosticLog
import com.edrl.stickerbridge.core.extraction.HostAllowlist
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Refuses any request of the app's own HTTP client that is not https to a TikTok domain (NFR5).
 * The image addresses come from TikTok's responses, so they are checked before use. Installed both
 * as an application interceptor (nothing is sent to a refused address) and as a network
 * interceptor (redirects are checked too).
 */
class AllowlistInterceptor(
    private val allowlist: HostAllowlist,
    private val log: DiagnosticLog,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        if (!allowlist.isAllowedUrl(url.toString())) {
            log.event(TAG, "request to ${url.scheme}://${url.host} refused")
            throw IOException("address not allowed: ${url.host}")
        }
        return chain.proceed(chain.request())
    }

    private companion object {
        const val TAG = "network"
    }
}
