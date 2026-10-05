package com.edrl.stickerbridge.core.extraction

import java.net.URI
import java.net.URISyntaxException

/**
 * The domains the in-app browser may contact while loading a TikTok post (BR-05), and the only
 * ones the app downloads images from (NFR5). Everything else, such as analytics or ads, is blocked.
 */
class HostAllowlist(
    domains: Set<String>,
) {
    private val domains = domains.map { it.lowercase() }.toSet()

    fun isAllowed(host: String): Boolean {
        val normalized = host.lowercase().trimEnd('.')
        if (normalized.isEmpty()) return false
        return domains.any { normalized == it || normalized.endsWith(".$it") }
    }

    /** Whether [url] is an https address on an allowed domain (NFR5). */
    fun isAllowedUrl(url: String): Boolean {
        val uri =
            try {
                URI(url)
            } catch (_: URISyntaxException) {
                null
            }
        return uri != null && uri.scheme.equals("https", ignoreCase = true) && isAllowed(uri.host.orEmpty())
    }

    companion object {
        /**
         * TikTok's site, its file network and its static content. Assumption: the page works with only
         * these; if the walking skeleton shows otherwise, the list grows to what is strictly needed.
         */
        val TIKTOK =
            HostAllowlist(
                setOf(
                    "tiktok.com",
                    "tiktokcdn.com",
                    "tiktokcdn-us.com",
                    "tiktokcdn-eu.com",
                    "tiktokv.com",
                    "tiktokw.us",
                    "ttwstatic.com",
                    "byteoversea.com",
                    "ibytedtos.com",
                    "ibyteimg.com",
                    "muscdn.com",
                ),
            )
    }
}
