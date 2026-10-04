package com.edrl.stickerbridge.core.extraction

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HostAllowlistTest {
    private val allowlist = HostAllowlist.TIKTOK

    @Test
    fun `allows the tiktok site and its subdomains`() {
        assertTrue(allowlist.isAllowed("www.tiktok.com"))
        assertTrue(allowlist.isAllowed("tiktok.com"))
        assertTrue(allowlist.isAllowed("m.tiktok.com"))
    }

    @Test
    fun `allows tiktok file and static content domains`() {
        assertTrue(allowlist.isAllowed("p16-sign.tiktokcdn.com"))
        assertTrue(allowlist.isAllowed("sf16-website-login.neutral.ttwstatic.com"))
    }

    @Test
    fun `is case insensitive`() {
        assertTrue(allowlist.isAllowed("WWW.TikTok.com"))
    }

    @Test
    fun `rejects other domains`() {
        assertFalse(allowlist.isAllowed("www.google-analytics.com"))
        assertFalse(allowlist.isAllowed("doubleclick.net"))
    }

    @Test
    fun `rejects lookalike domains that only end with the same letters`() {
        assertFalse(allowlist.isAllowed("eviltiktok.com"))
        assertFalse(allowlist.isAllowed("tiktok.com.evil.net"))
    }

    @Test
    fun `rejects empty hosts`() {
        assertFalse(allowlist.isAllowed(""))
    }
}
