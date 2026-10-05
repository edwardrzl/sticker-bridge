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

    @Test
    fun `allows https addresses on tiktok domains`() {
        assertTrue(allowlist.isAllowedUrl("https://p16-tiktok-dm-sticker-sign-sg.ibyteimg.com/a/b~c.awebp?x-expires=1"))
        assertTrue(allowlist.isAllowedUrl("https://p16-common-sign.tiktokcdn.com/photo.jpeg"))
    }

    @Test
    fun `rejects addresses without encryption`() {
        assertFalse(allowlist.isAllowedUrl("http://p16-common-sign.tiktokcdn.com/photo.jpeg"))
    }

    @Test
    fun `rejects addresses on other domains`() {
        assertFalse(allowlist.isAllowedUrl("https://example.com/photo.jpeg"))
        assertFalse(allowlist.isAllowedUrl("https://tiktokcdn.com.evil.net/photo.jpeg"))
    }

    @Test
    fun `rejects addresses that hide the real host behind user info`() {
        assertFalse(allowlist.isAllowedUrl("https://www.tiktok.com@evil.net/photo.jpeg"))
    }

    @Test
    fun `rejects other schemes and text that is not an address`() {
        assertFalse(allowlist.isAllowedUrl("file:///data/data/com.edrl.stickerbridge/files/packs/packs.json"))
        assertFalse(allowlist.isAllowedUrl("content://media/external/images/1"))
        assertFalse(allowlist.isAllowedUrl("not an address"))
        assertFalse(allowlist.isAllowedUrl(""))
    }
}
